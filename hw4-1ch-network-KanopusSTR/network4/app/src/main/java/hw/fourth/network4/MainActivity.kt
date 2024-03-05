package hw.fourth.network4

import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

data class Message(
    val id: Int,
    val from: String,
    val data: String?,
    var image: Bitmap? = null,
    var time: String = ""
)

val list = ArrayList<Message>()

const val myRequestID = 362615

class MainActivity : AppCompatActivity() {

    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var myService: MyService
    var mMyServiceIntent: Intent? = null
    var isBound = false

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (myService.getNew) {
                startService(mMyServiceIntent)
            } else {
                swipeContainer.isRefreshing = false
            }
            updateRecycler()
        }
    }

    private val boundServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binderBridge: MyService.MyBinder = service as MyService.MyBinder
            myService = binderBridge.getMyService()
            isBound = true
            startChat()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MyService::class.java)
        bindService(intent, boundServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(boundServiceConnection)
        isBound = false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myRecyclerView: RecyclerView = findViewById(R.id.myRecyclerView)

        mMyServiceIntent = Intent(this@MainActivity, MyService::class.java)

        swipeContainer = findViewById(R.id.swiperefresh)
        swipeContainer.setOnRefreshListener {
            getNew()
            updateRecycler()
            swipeContainer.isRefreshing = false
        }

        val down = findViewById<Button>(R.id.button2)
        down.setOnClickListener {
            myRecyclerView.scrollToPosition(list.size - 1)
        }

        val up = findViewById<Button>(R.id.button3)
        up.setOnClickListener {
            myRecyclerView.scrollToPosition(0)
        }

        val choose = findViewById<Button>(R.id.choose)
        choose.setOnClickListener {
            selectImageFromGallery()
            updateRecycler()
        }

        val textView = findViewById<EditText>(R.id.plain_text_input)
        val send = findViewById<Button>(R.id.button)
        send.setOnClickListener {
            val text = textView.text.toString()
            myService.sendText(text)
            textView.text.clear()
        }

        if (savedInstanceState != null) {
            isBound = savedInstanceState.getBoolean("mBound")
            updateRecycler()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("mBound", isBound)
    }

    fun updateRecycler() {
        val myRecyclerView: RecyclerView = findViewById(R.id.myRecyclerView)
        myRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun startChat() {
        swipeContainer.isRefreshing = true
        val viewManager = LinearLayoutManager(this)
        viewManager.stackFromEnd = true
        viewManager.reverseLayout = false
        val myRecyclerView: RecyclerView = findViewById(R.id.myRecyclerView)
        myRecyclerView.apply {
            layoutManager = viewManager
            adapter = UserAdapter(list) {
                if (it.image != null) {
                    val intent = Intent(this@MainActivity, ImageActivity::class.java)
                    val path = it.data
                    val url = "http://213.189.221.170:8008/img/$path"
                    startActivity(intent.putExtra("url string", url))
                }
            }
        }
        getNew()
    }

    private fun getNew() {
        startService(mMyServiceIntent)
        val intentFilter = IntentFilter("MessageService")
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                myService.sendImage(uri)
            }
        }

}


class UserViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    private val fromView: TextView = root.findViewById(R.id.from)
    private val dataView: TextView = root.findViewById(R.id.data)
    val imageView: ImageView = root.findViewById(R.id.message_image)
    private val timeView: TextView = root.findViewById(R.id.time)

    fun bind(user: Message) {
        fromView.text = user.from
        if (user.image == null) {
            dataView.text = user.data
        } else {
            dataView.text = null
        }
        timeView.text = user.time
        imageView.setImageBitmap(user.image)
    }
}

class UserAdapter(
    private val users: List<Message>,
    private val onClick: (Message) -> Unit
) : RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : UserViewHolder {
        val holder = UserViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
        holder.imageView.setOnClickListener {
            onClick(users[holder.adapterPosition])
        }
        return holder
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(users[position])

}
