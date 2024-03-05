package hw.sixth.hw6

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.util.*

class ChannelsViewModel : ViewModel() {
    private var lastInd = 100000
    private var maxID = 100000
    var list : MutableList<Message> = Collections.synchronizedList(mutableListOf())
    lateinit var dbPrivate : AppDatabase
    lateinit var adapter: ChannelsAdapter
    var listOfChats : MutableList<String> = Collections.synchronizedList(mutableListOf())

    fun getChannels() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = MyApp.instance.apiService.getChannels()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        listOfChats.addAll(response.body()!!)
                        retrieverList()
                    }
                }
                getFromDBPrivateChat()
            }
            catch (_: ConnectException) {
                withContext(Dispatchers.Main) {
                    listOfChats.add("1@channel")
                    retrieverList()
                }
            }
        }
    }

    private fun getFromDBPrivateChat() {
        CoroutineScope(Dispatchers.IO).launch {
            val listOfDB = dbPrivate.messageDao?.allPeople!!
            if (listOfDB.isNotEmpty()) {
                for (i in listOfDB.size - 1 downTo 0) {
                    if (!listOfChats.contains(listOfDB[i]!!.from) && !listOfChats.contains("Private chat with: " + listOfDB[i]!!.from) && listOfDB[i]!!.from != "Kan") {
                        listOfChats.add("Private chat with: " + listOfDB[i]!!.from)
                    } else if (!listOfChats.contains(listOfDB[i]!!.to) && !listOfChats.contains("Private chat with: " + listOfDB[i]!!.to) && listOfDB[i]!!.from != "Kan") {
                        listOfChats.add("Private chat with: " + listOfDB[i]!!.to)
                    }
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
                }
                withContext(Dispatchers.Main) {
                    retrieverList()
                }
                maxID = listOfDB[listOfDB.size - 1]!!.id
                getMessagesForPrivateChat()
            } else {
                getMessagesForPrivateChat()
            }
            dbPrivate.close()
        }
    }

    private fun getMessagesForPrivateChat(new : Boolean = false) {
        if (new) {
            lastInd = 10000
        } else {
            maxID = 2
        }
        val service = MyApp.instance.apiService
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (maxID == 2) {
                    maxID = MyApp.instance.apiService.getMessagesPrivateChats(0, 1, false).body()!![0].id!!.toInt()
                }
                val response = service.getMessagesPrivateChats(lastInd, 100)
                val tmpList = mutableListOf<Message>()
                for (i in response.body()!!) {
                    if (i.id?.toInt()!! > maxID) {
                        tmpList.add(i)
//                        maxID = i.id.toInt()
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
                        lastInd = list[list.size - 1].id!!.toInt()
                    }
                }
                for (i in tmpList) {
                    if (!listOfChats.contains(i.from) && !listOfChats.contains("Private chat with: " + i.from) && i.from != "Kan") {
                        listOfChats.add("Private chat with: " + i.from.toString())
                    } else if (!listOfChats.contains(i.to) && !listOfChats.contains("Private chat with: " + i.to) && i.from != "Kan") {
                        listOfChats.add("Private chat with: " + i.to.toString())
                    }
                    val msgDB = MessageDB(
                        i.id!!.toInt(),
                        i.from.toString(),
                        i.to.toString(),
                        i.data.Text?.text,
                        i.data.Image?.link,
                        i.time
                    )
                    dbPrivate.messageDao?.insert(msgDB)
                }
                withContext(Dispatchers.Main) {
                    retrieverList()
                }
                if (lastInd > maxID) {
                    getMessagesForPrivateChat(new)
                } else {
                    maxID = list[0].id!!.toInt()
                }
            } catch (_: Exception) {}
        }
    }

    private fun retrieverList() {
        adapter.apply {
            notifyDataSetChanged()
        }
    }

}