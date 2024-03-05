package hw.ninth.hw9_hilt.view

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import hw.ninth.hw9_hilt.R

@AndroidEntryPoint
class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        Picasso.get()
            .load(intent.getStringExtra("url"))
            .into(findViewById<ImageView>(R.id.imageIMG))
    }
}