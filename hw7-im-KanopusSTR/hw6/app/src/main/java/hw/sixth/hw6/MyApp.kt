package hw.sixth.hw6

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import android.app.Application
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("/channels")
    suspend fun getChannels(): Response<Array<String>>

    @GET("/channel/{channelName}")
    suspend fun getMessagesFromChannel(
        @Path("channelName") name: String,
        @Query("lastKnownId") last: Int,
        @Query("limit") limit: Int,
        @Query("reverse") reverse: Boolean = true
    ): Response<List<Message>>

    @Headers("Content-Type: application/json")
    @POST("1ch")
    fun sendMessage(@Body user: Message): Call<Message>

    @Multipart
    @POST("1ch")
    fun sendImage(
        @Part ("msg") msg : RequestBody,
        @Part file : MultipartBody.Part
    ): Call<ResponseBody>

    @GET("inbox/Kan")
    suspend fun getMessagesPrivateChats(
        @Query("lastKnownId") last: Int,
        @Query("limit") limit: Int,
        @Query("reverse") reverse: Boolean = true
    ): Response<List<Message>>
}

class MyApp : Application() {
    lateinit var apiService: ApiService

    override fun onCreate() {
        super.onCreate()
        instance = this
        apiService = Retrofit.Builder()
            .baseUrl("http://213.189.221.170:8008/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(OkHttpClient())
            .build()
            .create(ApiService::class.java)
    }

    companion object {
        lateinit var instance: MyApp
    }
}