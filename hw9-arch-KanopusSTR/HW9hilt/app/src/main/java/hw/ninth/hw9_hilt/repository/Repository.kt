package hw.ninth.hw9_hilt.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import hw.ninth.hw9_hilt.api.ApiService
import hw.ninth.hw9_hilt.model.*
import hw.ninth.hw9_hilt.constants.CHANNEL_NAME
import hw.ninth.hw9_hilt.constants.LIMIT
import hw.ninth.hw9_hilt.constants.USER_NAME
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
import java.util.*
import javax.inject.Inject

class Repository @Inject constructor(private val apiService: ApiService, private val db: MessageDao) {

    private var load: Boolean = false
    private var maxId: Int = 0
    val data: MutableLiveData<List<Message>?> = MutableLiveData<List<Message>?>()
    private val localData = Collections.synchronizedList<Message>(mutableListOf())

    suspend fun getMessages() {
        if (db.allMessage.isEmpty()) {
            getAllMessageList()
        } else {
            getAllMessageListDB()
        }
    }

    private suspend fun getAllMessageList(lastKnownId: Int = Int.MAX_VALUE) {
        try {
            val response = apiService.getMessages(lastKnownId, LIMIT)
            localData.addAll(response)
            data.postValue(localData)
            writeDB(response)
            if (response.isNotEmpty()) {
                getAllMessageList(localData[localData.lastIndex].id!!.toInt())
            } else {
                maxId = localData[0].id!!.toInt()
            }
        } catch (_: ConnectException) {
        }
    }

    suspend fun getNewMessages(curId: Int = Int.MAX_VALUE) {
        try {
            val tmpList = mutableListOf<Message>()
            val response = apiService.getMessages(curId, LIMIT)
            for (i in response) {
                if (i.id?.toInt()!! > maxId) {
                    tmpList.add(i)
                } else {
                    break
                }
            }
            if (load) {
                localData.addAll(0, tmpList)
                data.postValue(localData)
            }
            writeDB(tmpList)
            if (tmpList.size == LIMIT) {
                getNewMessages(tmpList[tmpList.lastIndex].id!!.toInt())
            } else {
                if (localData.isNotEmpty()) {
                    maxId = localData[0].id?.toInt()!!
                }
                if (!load) {
                    getFromDB()
                }
            }
        } catch (ex: ConnectException) {
            if (!load) {
                getFromDB()
            }
        }
    }

    private suspend fun getAllMessageListDB() {
        maxId = db.allMessage[db.allMessage.lastIndex]!!.id
        load = false
        getNewMessages()
    }

    private fun getFromDB() {
        for (i in db.allMessage) {
            val tmpData: Data1 = if (i!!.url == null) {
                Data1(Data2(i.text, null), null)
            } else {
                Data1(null, Data2(null, i.url))
            }
            localData.add(
                0, Message(
                    i.id.toString(),
                    i.from,
                    i.to,
                    tmpData,
                    i.time
                )
            )
            if (localData.size % 100 == 0) {
                data.postValue(localData)
            }
        }
        load = true
    }

    private fun writeDB(messages: List<Message>) {
        for (i in messages) {
            db.insert(
                MessageDB(
                    i.id!!.toInt(),
                    i.from.toString(),
                    i.to.toString(),
                    i.data1.text?.text,
                    i.data1.image?.link,
                    i.time
                )
            )
        }
    }

    fun sendText(text: String) {
        apiService.sendMessage(
            Message(
                null,
                USER_NAME,
                CHANNEL_NAME,
                Data1(Data2(text, null), null),
                null
            )
        ).enqueue(
            object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    errorHandler(response)
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Log.e("Upload error:", t.message!!)
                }
            }
        )
    }

    fun loadImage(uri: Uri, context: Context): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File("${context.filesDir}/${System.currentTimeMillis()}.jpg")
        val outputStream: OutputStream = FileOutputStream(file)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream?.read(buf).also { len = it!! }!! > 0) {
            outputStream.write(buf, 0, len)
        }
        outputStream.close()
        inputStream?.close()
        return file
    }

    fun sendImage(file: File, type: String) {
        val description: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            "{\"from\":\"Kan\"," +
                    "\"to\":\"1@channel\"," +
                    "\"data\":{\"Image\":{\"link\":\"${file.absolutePath}\"}}}"
        )

        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse(type),
            file
        )

        val body = MultipartBody.Part.createFormData(
            "picture",
            file.name,
            requestFile
        )

        val sendImageCall: Call<ResponseBody> = apiService.sendImage(description, body)
        sendImageCall.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                errorHandler(response)
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("Upload error:", t.message!!)
            }
        })
    }

    fun errorHandler(response: Response<ResponseBody?>) {
        if (response.code() >= 500) {
            System.err.println(
                "Response code " + response.code()
                    .toString() + " The error was caused by the server"
            )
        } else if (response.code() == 413) {
            System.err.println(
                "Response code " + response.code().toString() + " Message too big for sending"
            )
        } else if (response.code() == 409) {
            System.err.println("Response code " + response.code().toString() + " Try one more time")
        } else if (response.code() == 404) {
            System.err.println(
                "Response code " + response.code()
                    .toString() + " The server cannot find the requested resource"
            )
        }
    }
}