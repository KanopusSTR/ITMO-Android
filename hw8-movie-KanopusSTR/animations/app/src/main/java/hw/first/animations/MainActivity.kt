package hw.first.animations

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.BounceInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Runnable

class MainActivity : AppCompatActivity() {
    lateinit var indicator: MyCustomIndicator
    val main = Handler(Looper.getMainLooper())

    private val timer = Timer(200)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        indicator = findViewById(R.id.indicator)

        val word = findViewById<TextView>(R.id.text)
        val runnable1: Runnable?
        var runnable2: Runnable? = null
        runnable1 = Runnable {
            word.animate()
                .scaleX(0.3f)
                .y(0f)
                .setDuration(1500)
                .setInterpolator(BounceInterpolator())
                .withEndAction(runnable2)
                .start()
        }
        runnable2 = Runnable {
            word.animate()
                .scaleX(1.5f)
                .setDuration(1500)
                .setInterpolator(BounceInterpolator())
                .withEndAction(runnable1)
                .start()
        }
        runnable1.run()
    }

    override fun onStart() {
        super.onStart()
        main.post(timer)
    }

    override fun onStop() {
        super.onStop()
        main.removeCallbacks(timer)
    }

    inner class Timer(delay: Int) : java.lang.Runnable {
        private var delay = 0


        init {
            this.delay = delay
        }

        override fun run() {
            indicator.nextFrame()
            main.postDelayed(this, delay.toLong())
        }
    }
}