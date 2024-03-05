package hw.sixth.hw6

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import hw.sixth.hw6.databinding.ItemLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainAdapter(private val users: MutableList<Message>, private val onClick: (Message) -> Unit) : RecyclerView.Adapter<MainAdapter.DataViewHolder>() {

    val myDataFormat = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())

    inner class DataViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Message) {
            itemView.apply {
                if (msg.time != null) {
                    binding.time.text = myDataFormat.format(msg.time.toLong())
                }
                binding.from.text = msg.from
                if (msg.data.Text != null) {
                    binding.data.text = msg.data.Text.text
                    binding.messageImage.setImageBitmap(null)
                    Picasso.get().cancelRequest(binding.messageImage)
                } else if (msg.data.Image != null) {
                    val myDir = File(Environment.getExternalStorageDirectory().toString() + "/saveImageFor_6/7_hw")
                    if (!myDir.exists()) {
                        myDir.mkdirs()
                    }
                    val fName = msg.id.toString() + ".jpg"
                    val file = File(myDir, fName)
                    if (file.exists()) {
                        Picasso.get()
                            .load(file)
                            .into(binding.messageImage)
                    } else {
                        Picasso.get()
                            .load("http://213.189.221.170:8008/thumb/" + msg.data.Image.link)
                            .into(binding.messageImage)
                        Picasso.get()
                            .load("http://213.189.221.170:8008/thumb/" + msg.data.Image.link)
                            .into(getTarget(msg.id.toString() +  ".jpg"))
                    }
                    binding.data.text = ""
                }
            }
        }

        private fun getTarget(path: String): com.squareup.picasso.Target {
            return object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        kotlin.runCatching {
                            val file =
                                File(Environment.getExternalStorageDirectory().path + "/saveImageFor_6/7_hw" + path)
                            if (!File(file.parent!!).exists()) {
                                File(file.parent!!).createNewFile()
                            }
                            file.createNewFile()
                            val ostream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, ostream)
                            ostream.flush()
                            ostream.close()
                        }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = DataViewHolder(binding)
        binding.messageImage.setOnClickListener {
            onClick(users[holder.adapterPosition])
        }
        return holder
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(users[position])
    }
}