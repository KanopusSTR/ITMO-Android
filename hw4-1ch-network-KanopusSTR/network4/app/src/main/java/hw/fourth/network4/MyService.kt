package hw.fourth.network4

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MyService : Service() {
    var getNew = false
    private var lastKnownId = 0
    private var count = 0

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        getNew = false
        url = intent.getStringExtra("url string")
        Thread {
            readAll()
        }.start()
        val responseIntent = Intent("MessageService")
        responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        sendBroadcast(responseIntent)
        return START_NOT_STICKY
    }

    override fun onBind(arg0: Intent): IBinder {
        return MyBinder()
    }

    inner class MyBinder : Binder() {
        fun getMyService() = this@MyService
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun getJSON(url: URL): JsonArray {
        var line = ""
        var answer: JsonArray
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val `in` = BufferedReader(
                    InputStreamReader(
                        connection.inputStream
                    )
                )
                line = `in`.readText()
                `in`.close()
            }
            answer = JsonParser().parse(line).asJsonArray
        } catch (e: IOException) {
            answer = JsonArray()
        }
        return answer
    }

    private fun getImageTHUMB(url: URL): Bitmap? {
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            val a = if (responseCode == HttpURLConnection.HTTP_OK) {
                val `in` = connection.inputStream
                val image: Bitmap = BitmapFactory.decodeStream(`in`)
                `in`.close()
                image
            } else {
                null
            }
            return a
        } catch (_: IOException) {
            return null
        }
    }

    var imageIMG: Bitmap? = null

    private var url: String? = null

    fun getImageIMG(url: String) {
        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode
                val a = if (responseCode == HttpURLConnection.HTTP_OK) {
                    val `in` = connection.inputStream
                    val image: Bitmap = BitmapFactory.decodeStream(`in`)
                    image
                } else {
                    null
                }
                mainHandler.post {
                    imageIMG = a
                }
            } catch (_: IOException) {

            }
            val responseIntent = Intent("ImageService")
            responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendBroadcast(responseIntent)
        }.start()
    }

    fun sendText(text: String) {
        Thread {
            try {
                val url = URL("http://213.189.221.170:8008/1ch")
                val connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                val outputStreamWriter: OutputStreamWriter = connection.outputStream.writer()
                outputStreamWriter.append("{\"from\":\"Kan\",\"to\":\"1@channel\",\"data\":{\"Text\":{\"text\":\"$text\"}}}")
                    .flush()
                outputStreamWriter.close()
                val code = connection.responseCode
                if (code in 200..299) System.out else System.err
                connection.disconnect()
                getNew = true
            } catch (_: IOException) { }
            val responseIntent = Intent("MessageService")
            responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendBroadcast(responseIntent)
        }.start()
    }

    fun sendImage(uri: Uri) {
        Thread {
            val `in`: InputStream? = contentResolver.openInputStream(uri)
            val tmpFile = File("${filesDir}/${System.currentTimeMillis()}.jpg")
            val path = tmpFile.absolutePath
            val out: OutputStream = FileOutputStream(tmpFile)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`?.read(buf).also { len = it!! }!! > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            `in`?.close()
            try {
                MultipartExampleClient.main(
                    arrayOf(
                        "http://213.189.221.170:8008/1ch",
                        "msg%{\"from\":\"Kan\",\"to\":\"1@channel\",\"data\":{\"Image\":{\"link\":\"$path\"}}}",
                        "pic=$path"
                    )
                )
                getNew = true
            } catch (_: IOException) { }
            val responseIntent = Intent("MessageService")
            responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendBroadcast(responseIntent)
        }.start()
    }

    data class ImageURL(val url: URL, val number: Int)
    data class ImageBitMap(val bitmap: Bitmap?, val number: Int)

    private var urlQueue: Queue<ImageURL> = ArrayDeque()

    private fun loadImage() {
        val imageQueue: Queue<ImageBitMap> = ArrayDeque()
        Thread {
            val ab = urlQueue.size
            for (i in 0 until ab) {
                val image = urlQueue.poll()
                if (image != null) {
                    imageQueue.add(ImageBitMap(getImageTHUMB(image.url), image.number))
                }
            }
            mainHandler.post {
                for (elem in imageQueue) {
                    if (elem.number < list.size) {
                        val image = imageQueue.poll()
                        if (image != null) {
                            list[image.number].image = image.bitmap
                        }
                    }
                }
            }
        }.start()
        val responseIntent = Intent("MessageService")
        responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        sendBroadcast(responseIntent)
    }

    private fun readAll() {
        var good = true
        val myDataFormat = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
        while (good) {
            val url = URL("http://213.189.221.170:8008/1ch?limit=100&lastKnownId=$lastKnownId")

            val a = getJSON(url)
            val size = a.size()
            count += size
            if (size == 0) {
                break
            }
            if (size < 100) {
                good = false
            }

            mainHandler.post {
                var elem = a[0]
                for (b: Int in 0 until size) {
                    elem = a[b]
                    val str: String = if (elem.asJsonObject["data"].asJsonObject.toString()
                            .substring(2, 6) == "Text"
                    ) {
                        elem.asJsonObject["data"].asJsonObject["Text"].asJsonObject["text"].asString.toString()
                    } else {
                        elem.asJsonObject["data"].asJsonObject["Image"].asJsonObject["link"].asString.toString()
                    }
                    val time = myDataFormat.format(
                        elem.asJsonObject["time"].asString.toString().toLong()
                    )
                    list.add(
                        Message(
                            elem.asJsonObject["id"].asInt,
                            elem.asJsonObject["from"].asString.toString(),
                            str,
                            null,
                            time
                        )
                    )

                    if (elem.asJsonObject["data"].asJsonObject.toString()
                            .substring(2, 6) != "Text"
                    ) {
                        val pict =
                            elem.asJsonObject["data"].asJsonObject["Image"].asJsonObject["link"].asString.toString()
                        val url2 = URL("http://213.189.221.170:8008/thumb/$pict")
                        urlQueue.add(ImageURL(url2, count + b - size))
                    }
                }
                lastKnownId = elem.asJsonObject["id"].asInt
                loadImage()
            }
            val responseIntent = Intent("MessageService")
            responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendBroadcast(responseIntent)
        }
        val responseIntent = Intent("MessageService")
        responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        sendBroadcast(responseIntent)
    }
}