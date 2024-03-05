package hw.ninth.hw9_hilt.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import hw.ninth.hw9_hilt.constants.BASE_URL
import hw.ninth.hw9_hilt.databinding.ActivityMainBinding
import hw.ninth.hw9_hilt.model.Message
import hw.ninth.hw9_hilt.viewModel.MainViewModel
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: MainAdapter
    private lateinit var send: TextView
    private lateinit var getImage: TextView
    private lateinit var refresh: Button
    private var list: MutableList<Message> = Collections.synchronizedList(mutableListOf())
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        checkAndRequestPermissions()
        val viewModel1: MainViewModel by viewModels { MainViewModel.Factory }
        viewModel = viewModel1
        val mLayoutManager = LinearLayoutManager(this)
        mLayoutManager.reverseLayout = true
        binding.myRecyclerView.layoutManager = mLayoutManager

        adapter = MainAdapter(list) {
            if (it.data1.image != null) {
                val intent = Intent(this@MainActivity, ImageActivity::class.java)
                intent.putExtra("url", BASE_URL + "thumb/" + it.data1.image.link)
                startActivity(intent)
            }
        }
        binding.myRecyclerView.adapter = adapter

        viewModel.liveDataMessageList.observe(
            this
        ) { lists ->
            if (lists != null) {
                list.clear()
                list.addAll(lists)
                adapter.notifyDataSetChanged()
            }
        }

        send = binding.button
        send.setOnClickListener {
            val editText = binding.plainTextInput
            viewModel.sendText(editText.text.toString())
            editText.text.clear()
        }
        getImage = binding.choose
        getImage.setOnClickListener {
            if (checkAndRequestPermissions()) {
                sendImageFromGallery()
            }
        }
        refresh = binding.refresh
        refresh.setOnClickListener {
            viewModel.updateData()
        }
    }

    private fun sendImageFromGallery() = sendImageFromGalleryResult.launch("image/*")

    private val sendImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = viewModel.loadImage(uri, this)
                viewModel.sendImage(file, contentResolver.getType(uri)!!)
            }
        }

    private fun checkAndRequestPermissions(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }
}