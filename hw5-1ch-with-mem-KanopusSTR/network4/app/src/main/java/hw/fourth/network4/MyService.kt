package hw.fourth.network4

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import androidx.room.Room
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
    lateinit var db: MainActivity.AppDatabase
    var end = false
    private var serviceHandler: ServiceHandler? = null
    var lastDB = 0


    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: android.os.Message) {
            if (getNew) {
                readAll()
                val responseIntent = Intent("MessageService")
                responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                sendBroadcast(responseIntent)
            } else {
                db = Room.databaseBuilder(
                    applicationContext,
                    MainActivity.AppDatabase::class.java, dbName
                ).build()
                val listOfDB = db.messageDao?.all!!
                for (i in lastDB until listOfDB.size) {
                    var img: Bitmap? = null
                    if (listOfDB[i]?.textOrImg == false) {
                        val imgPath =
                            Environment.getExternalStorageDirectory().path + "/saved1" + "/${listOfDB[i]!!.id}.jpg"
                        val options = BitmapFactory.Options()
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888
                        img = BitmapFactory.decodeFile(imgPath, options)
                    }
                    val message = Message(
                        listOfDB[i]!!.id,
                        listOfDB[i]!!.from,
                        listOfDB[i]!!.data,
                        img,
                        listOfDB[i]!!.textOrImg,
                        listOfDB[i]!!.time,
                    )
                    if (message !in list) {
                        list.add(message)
                        lastDB = i + 1
                        count = db.messageDao?.size()!!
                        if (count % 100 == 0) {
                            val responseIntent = Intent("MessageService")
                            responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            sendBroadcast(responseIntent)
                        }
                    }
                }
                getNew = true
                val responseIntent = Intent("MessageService")
                responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                sendBroadcast(responseIntent)
            }
        }
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments").apply {
            start()
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        url = intent.getStringExtra("url string")
        serviceHandler?.obtainMessage()?.also { msg ->
            serviceHandler?.sendMessage(msg)
        }
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
            } catch (_: IOException) {
            }
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
            } catch (_: IOException) {
            }
            val responseIntent = Intent("MessageService")
            responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendBroadcast(responseIntent)
        }.start()
    }

    data class ImageURL(val url: URL, val number: Int, val id: Int)
    data class ImageBitMap(val bitmap: Bitmap?, val number: Int, val id: Int)

    private var urlQueue: Queue<ImageURL> = ArrayDeque()

    private fun loadImage() {
        val imageQueue: Queue<ImageBitMap> = ArrayDeque()
        Thread {
            val ab = urlQueue.size
            for (i in 0 until ab) {
                val image = urlQueue.poll()
                if (image != null) {
                    imageQueue.add(ImageBitMap(getImageTHUMB(image.url), image.number, image.id))
                }
            }
            for (elem in imageQueue) {
                mainHandler.post {
                    if (elem.number < list.size) {
                        val image = imageQueue.poll()
                        if (image != null) {
                            list[image.number].image = image.bitmap
                        }
                    }
                }
                val myDir = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + "/saved1"
                )
                if (!myDir.exists()) {
                    myDir.mkdirs()
                }
                val fName = elem.id.toString() + ".jpg"
                val file = File(myDir, fName)
                if (file.exists()) {
                    file.delete()
                }
                try {
                    if (!myDir.exists()) {
                        myDir.mkdirs()
                    }
                    file.createNewFile()
                    val out = FileOutputStream(file)
                    val bm = list[elem.number].image
                    bm?.compress(Bitmap.CompressFormat.JPEG, 50, out)
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("some do not saved " + list[elem.number].id.toString())
                }
            }
        }.start()
    }

    private fun readAll() {
        db = Room.databaseBuilder(
            applicationContext,
            MainActivity.AppDatabase::class.java, dbName
        ).build()
        if (db.messageDao?.lastID()!! > lastKnownId) {
            lastKnownId = db.messageDao?.lastID()!!
        }
        val myDataFormat = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())

        var good = true
        while (good) {
            val url = URL("http://213.189.221.170:8008/1ch?limit=100&lastKnownId=$lastKnownId")
            val a = getJSON(url)
            val size = a.size()
            if (size == 0) {
                end = true
                break
            }
            if (size < 100) {
                good = false
            }
            val tmpList: ArrayList<Message> = ArrayList()
            for (b: Int in 0 until size) {
                val elem = a[b]
                lastKnownId = elem.asJsonObject["id"].asInt
                val str: String
                val bdINd: Boolean
                var textOrPict = true
                if (elem.asJsonObject["data"].asJsonObject.toString()
                        .substring(2, 6) == "Text"
                ) {
                    str =
                        elem.asJsonObject["data"].asJsonObject["Text"].asJsonObject["text"].asString.toString()
                    bdINd = true
                } else {
                    textOrPict = false
                    str =
                        elem.asJsonObject["data"].asJsonObject["Image"].asJsonObject["link"].asString.toString()
                    bdINd = false
                    val pict =
                        elem.asJsonObject["data"].asJsonObject["Image"].asJsonObject["link"].asString.toString()
                    val url2 = URL("http://213.189.221.170:8008/thumb/$pict")
                    urlQueue.add(ImageURL(url2, count, lastKnownId))
                }
                val time = myDataFormat.format(
                    elem.asJsonObject["time"].asString.toString().toLong()
                )
                tmpList.add(
                    Message(
                        elem.asJsonObject["id"].asInt,
                        elem.asJsonObject["from"].asString.toString(),
                        str,
                        null,
                        textOrPict,
                        time
                    )
                )
                val id = elem.asJsonObject["id"].asInt
                db.messageDao?.insert(
                    MainActivity.MessageDB(
                        id,
                        elem.asJsonObject["from"].asString.toString(),
                        str,
                        bdINd,
                        time
                    )
                )
                count += 1
            }
            loadImage()
            mainHandler.post {
                list.addAll(tmpList)
            }
        }
    }
}