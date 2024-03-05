package hw.ninth.hw9_hilt.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import hw.ninth.hw9_hilt.repository.Repository
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    @Inject
    lateinit var repository: Repository
}