package hw.fourth.network4

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView

class ImageActivity : AppCompatActivity() {
    private lateinit var myService: MyService
    var mMyServiceIntent: Intent? = null
    var isBound = false
    private var broadcastReceiver: BroadcastReceiver? = null

    var url = ""

    private val boundServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binderBridge: MyService.MyBinder = service as MyService.MyBinder
            myService = binderBridge.getMyService()
            isBound = true
            myService.getImageIMG(url)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        url = intent.getStringExtra("url string").toString()

        mMyServiceIntent = Intent(this, MyService::class.java)

        val image: ImageView = findViewById(R.id.imageIMG)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                image.setImageBitmap(myService.imageIMG)
            }
        }

        val intentFilter = IntentFilter("ImageService")
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(broadcastReceiver, intentFilter)
        startService(mMyServiceIntent)
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
}