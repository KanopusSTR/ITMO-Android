package hw.ninth.hw9_hilt.view

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vdurmont.emoji.EmojiManager
import hw.ninth.hw9_hilt.constants.*
import hw.ninth.hw9_hilt.databinding.ListItemBinding
import hw.ninth.hw9_hilt.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainAdapter(
    private val messages: MutableList<Message>,
    private val onClick: (Message) -> Unit
) : RecyclerView.Adapter<MainAdapter.DataViewHolder>() {

    inner class DataViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private val myDataFormat = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())

        fun bind(msg: Message) {
            itemView.apply {
                binding.from.text = msg.from
                binding.time.text = myDataFormat.format(msg.time?.toLong())
                if (msg.data1.text != null) {
                    binding.data.text = msg.data1.text.text
                    binding.messageImage.setImageBitmap(null)
                    if (EmojiManager.isEmoji(msg.data1.text.text))
                    {
                        binding.data.textSize = 100F
                        binding.data.gravity = 1
                    } else {
                        binding.data.textSize = 18F
                        binding.data.gravity = 0
                    }
                    Picasso.get().cancelRequest(binding.messageImage)
                } else if (msg.data1.image != null) {
                    val myDir = File(
                        Environment.getExternalStorageDirectory().toString() + PATH_TO_SAVED_IMAGES
                    )
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
                            .load(BASE_URL + "thumb/" + msg.data1.image.link)
                            .into(binding.messageImage)
                        Picasso.get()
                            .load(BASE_URL + "thumb/" + msg.data1.image.link)
                            .into(getTarget("/$fName"))
                    }
                    binding.data.text = ""
                    binding.data.textSize = 0F
                    binding.data.gravity = 0
                }
            }
        }

        private fun getTarget(path: String): com.squareup.picasso.Target {
            return object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        kotlin.runCatching {
                            val file =
                                File(Environment.getExternalStorageDirectory().path + PATH_TO_SAVED_IMAGES + path)
                            if (!File(file.parent!!).exists()) {
                                File(file.parent!!).createNewFile()
                            }
                            file.createNewFile()
                            val outStream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream)
                            outStream.flush()
                            outStream.close()
                        }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = DataViewHolder(binding)
        binding.messageImage.setOnClickListener {
            onClick(messages[holder.adapterPosition])
        }
        return holder
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(messages[position])
    }
}