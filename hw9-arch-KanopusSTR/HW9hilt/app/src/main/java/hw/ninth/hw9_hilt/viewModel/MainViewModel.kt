package hw.ninth.hw9_hilt.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import hw.ninth.hw9_hilt.application.MyApplication
import hw.ninth.hw9_hilt.model.Message
import hw.ninth.hw9_hilt.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class MainViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return MainViewModel(
                    (application as MyApplication).repository
                ) as T
            }
        }
    }

    var liveDataMessageList: LiveData<List<Message>?> = repository.data.also {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getMessages()
        }
    }


    fun updateData() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getNewMessages()
        }
    }

    fun loadImage(uri: Uri, context: Context): File {
        return repository.loadImage(uri, context)
    }

    fun sendImage(file: File, type: String) {
        repository.sendImage(file, type)
    }

    fun sendText(text: String) {
        repository.sendText(text)
    }

}
