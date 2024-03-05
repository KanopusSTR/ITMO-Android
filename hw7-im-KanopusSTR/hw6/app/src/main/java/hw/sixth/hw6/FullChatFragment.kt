package hw.sixth.hw6

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import hw.sixth.hw6.databinding.ChatListItemBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException

class FullChatFragment : Fragment() {
    private lateinit var chatPath: String
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: MainAdapter
    private lateinit var binding: ChatListItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatPath = arguments?.getString(KEY_CHAT_PATH) ?: ""
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val str: String = if (chatPath.startsWith("Private chat with: ")) {
            privateDBName
        } else {
            "database_saved_text_for_channel_$chatPath"
        }
        viewModel.db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java, str
        ).build()
        viewModel.chatPath = chatPath
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatListItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        binding.swiperefresh.setOnRefreshListener {
            viewModel.getMessages(true)
            binding.swiperefresh.isRefreshing = false
        }
        binding.button.setOnClickListener {
            viewModel.postMessageText(binding.plainTextInput.text.toString())
            binding.plainTextInput.text.clear()
        }
        binding.choose.setOnClickListener {
            sendImageFromGallery()
        }
        binding.refresh.setOnClickListener {
            binding.swiperefresh.isRefreshing = true
            try {
                viewModel.getMessages(true)
            } catch (_: ConnectException) { }
            binding.swiperefresh.isRefreshing = false
        }
        viewModel.adapter = adapter
        viewModel.getFromDB()
    }

    private fun sendImageFromGallery() = sendImageFromGalleryResult.launch("image/*")

    private val sendImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                sendImage(uri)
            }
        }

    private fun sendImage(uri: Uri) {
        val `in`: InputStream = requireContext().contentResolver.openInputStream(uri)!!
        val file = File("${requireContext().filesDir}/${System.currentTimeMillis()}.jpg")
        val out: OutputStream = FileOutputStream(file)
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        out.close()
        `in`.close()

        var str = chatPath
        if (chatPath.startsWith("Private chat with: ")) {
            str = str.substring(19)
        }

        val description: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            "{\"from\":\"Kan\"," +
                    "\"to\":\"$str\"," +
                    "\"data\":{\"Image\":{\"link\":\"${file.absolutePath}\"}}}"
        )

        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse(requireContext().contentResolver.getType(uri)!!),
            file
        )

        val body = MultipartBody.Part.createFormData(
            "picture",
            file.name,
            requestFile
        )

        val call: Call<ResponseBody> = MyApp.instance.apiService.sendImage(description, body)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                if (response.code() >= 500) {
                    System.err.println(
                        "Response code " + response.code()
                            .toString() + " The error was caused by the server"
                    )
                } else if (response.code() == 413) {
                    System.err.println(
                        "Response code " + response.code().toString() + " Image too big for sending"
                    )
                } else if (response.code() == 409) {
                    sendImage(uri)
                } else if (response.code() == 404) {
                    System.err.println(
                        "Response code " + response.code()
                            .toString() + " The server cannot find the requested resource"
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("Upload error:", t.message!!)
            }
        })
    }

    private fun setupUI() {
        val mLayoutManager = LinearLayoutManager(context)
        mLayoutManager.reverseLayout = true
        binding.myRecyclerView.layoutManager = mLayoutManager
        adapter = MainAdapter(viewModel.list) {
            if (it.data.Image != null) {
                val intent = Intent(context, ImageActivity::class.java)
                intent.putExtra("url", "http://213.189.221.170:8008/thumb/" + it.data.Image.link)
                startActivity(intent)
            }
        }
        viewModel.adapter = adapter
        binding.myRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.myRecyclerView.context,
                (binding.myRecyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )
        binding.myRecyclerView.adapter = adapter
    }

    companion object {
        private const val KEY_CHAT_PATH = "chatPath"

        fun create(imagePath: String): FullChatFragment {
            return FullChatFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_CHAT_PATH, imagePath)
                }
            }
        }
    }
}