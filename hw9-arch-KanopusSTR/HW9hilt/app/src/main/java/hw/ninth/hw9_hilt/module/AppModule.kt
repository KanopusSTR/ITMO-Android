package hw.ninth.hw9_hilt.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hw.ninth.hw9_hilt.api.ApiService
import hw.ninth.hw9_hilt.constants.*
import hw.ninth.hw9_hilt.model.AppDatabase
import hw.ninth.hw9_hilt.model.MessageDao
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(MoshiConverterFactory.create())
            .build().create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideMessageDao(@ApplicationContext app: Context): MessageDao =
        Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "DATABASE_NAME"
        ).build().messageDao!!
}