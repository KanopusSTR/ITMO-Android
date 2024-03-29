package hw.ninth.hw9_hilt.api


import hw.ninth.hw9_hilt.model.Message
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("1ch")
    suspend fun getMessages(
        @Query("lastKnownId") lastKnownId: Int,
        @Query("limit") limit: Int,
        @Query("reverse") reverse: Boolean = true
    ): List<Message>

    @Headers("Content-Type: application/json")
    @POST("1ch")
    fun sendMessage(@Body msg: Message): Call<ResponseBody>

    @Multipart
    @POST("1ch")
    fun sendImage(
        @Part ("msg") msg : RequestBody,
        @Part file : MultipartBody.Part
    ): Call<ResponseBody>

}