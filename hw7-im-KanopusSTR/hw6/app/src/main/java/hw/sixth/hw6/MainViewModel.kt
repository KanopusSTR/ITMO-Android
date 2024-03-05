package hw.sixth.hw6

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException
import java.util.*

class MainViewModel : ViewModel() {
    lateinit var adapter: MainAdapter
    private var lastInd = 100000
    private var maxID = 100000
    var list : MutableList<Message> = Collections.synchronizedList(mutableListOf())
    lateinit var db : AppDatabase

    var chatPath : String = ""

    fun getMessages(new : Boolean = false) {
        if (chatPath.startsWith("Private chat with: ")) {
            getForPrivateChat(new)
        } else {
            if (new) {
                lastInd = 10000
            } else {
                maxID = 2
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (maxID == 2) {
                        maxID = MyApp.instance.apiService.getMessagesFromChannel(chatPath, 0, 1, false).body()!![0].id!!.toInt()
                    }
                    val response = MyApp.instance.apiService.getMessagesFromChannel(chatPath, lastInd, 100)
                    val tmpList = mutableListOf<Message>()
                    for (i in response.body()!!) {
                        if (i.id?.toInt()!! > maxID) {
                            tmpList.add(i)
//                            maxID = i.id.toInt()
                        } else {
                            break
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            if (new) {
                                list.addAll(0, tmpList)
                            } else {
                                list.addAll(response.body()!!)
                            }
                            retrieverList()
                            lastInd = list[list.size - 1].id!!.toInt()
                        }
                    }
                    for (i in tmpList) {
                        val msgDB = MessageDB(
                            i.id!!.toInt(),
                            i.from.toString(),
                            i.to.toString(),
                            i.data.Text?.text,
                            i.data.Image?.link,
                            i.time.toString()
                        )
                        if (db.messageDao?.getById(i.id.toInt()) == null) {
                            db.messageDao?.insert(msgDB)
                        }
                    }
                    if (lastInd > maxID) {
                        getMessages(new)
                    } else {
                        maxID = list[0].id!!.toInt()
                    }
                } catch (_: ConnectException) {}
            }
        }
    }

    fun postMessageText(text: String) {
        val service = MyApp.instance.apiService
        var str = chatPath
        if (str.startsWith("Private chat with: ")) {
            str = str.substring(19)
        }
        service.sendMessage(Message(null, "Kan", str, Data(SomeData(text, null), null), null)).enqueue(
            object : Callback<Message> {
                override fun onResponse(call: Call<Message>, response: Response<Message>) {
                    if (response.isSuccessful) {
                        println(response.body().toString())
                    } else {
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<Message>, t: Throwable) {
                    Log.e("Upload error:", t.message!!)
                }
            }
        )
    }

    private fun getForPrivateChat(new : Boolean = false) {
        if (new) {
            lastInd = 10000
        } else {
            maxID = 2
        }
        val service = MyApp.instance.apiService
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (maxID == 2) {
                    maxID = service.getMessagesPrivateChats( 0, 1, false).body()!![0].id!!.toInt()
                }
                val response = service.getMessagesPrivateChats(lastInd, 100)
                val tmpList = mutableListOf<Message>()
                for (i in response.body()!!) {
                    lastInd = kotlin.math.min(lastInd, i.id!!.toInt())
                    if (i.id.toInt() > maxID) {
                        if ((i.from == "Kan" && i.to == chatPath.substring(19)) ||
                            (i.to == "Kan" && i.from == chatPath.substring(19))
                        ) {
                            tmpList.add(i)
                        }
                    } else {
                        break
                    }
                }
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        if (new) {
                            list.addAll(0, tmpList)
                        } else {
                            for (i in response.body()!!) {
                                if ((i.from == "Kan" && i.to == chatPath.substring(19)) ||
                                    (i.to == "Kan" && i.from == chatPath.substring(19))
                                ) {
                                    list.add(i)
                                }
                            }
                        }
                        retrieverList()
                    }
                }
                if (lastInd > maxID) {
                    getMessages(new)
                } else if (list.size != 0) {
                    maxID = list[0].id!!.toInt()
                }
            } catch (_: ConnectException) {}
        }
    }

    fun getFromDB() {
        if (chatPath.startsWith("Private chat with: ")) {
            getForPrivateChat()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val listOfDB = db.messageDao?.allPeople!!
                if (listOfDB.isNotEmpty()) {
                    for (i in listOfDB.size - 1 downTo 0) {
                        if (listOfDB[i]!!.url == null) {
                            list.add(
                                Message(
                                    listOfDB[i]!!.id.toString(),
                                    listOfDB[i]!!.from,
                                    listOfDB[i]!!.to,
                                    Data(SomeData(listOfDB[i]!!.text, null), null),
                                    listOfDB[i]!!.time
                                )
                            )
                        } else {
                            list.add(
                                Message(
                                    listOfDB[i]!!.id.toString(),
                                    listOfDB[i]!!.from,
                                    listOfDB[i]!!.to,
                                    Data(null, SomeData(null, listOfDB[i]!!.url)),
                                    listOfDB[i]!!.time
                                )
                            )
                        }
                        if (i % 100 == 0) {
                            withContext(Dispatchers.Main) {
                                retrieverList()
                            }
                        }
                    }
                    maxID = listOfDB[listOfDB.size - 1]!!.id + 1
                } else {
                    getMessages()
                }
                db.close()
            }
        }
    }

    private fun retrieverList() {
        adapter.apply {
            notifyDataSetChanged()
        }
    }

}