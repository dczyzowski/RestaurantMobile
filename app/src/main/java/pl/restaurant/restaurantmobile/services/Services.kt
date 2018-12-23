package pl.restaurant.restaurantmobile.services

import pl.restaurant.restaurantmobile.models.Times
import retrofit2.Call
import okhttp3.ResponseBody
import pl.restaurant.restaurantmobile.models.User
import retrofit2.http.*


interface GetTime {
    @GET("work_time/daily_time/")
    fun getTimes(@Header("Authorization") authHeader: String) : Call<Times>
}

interface UpdateTime {
    @PATCH("work_time/daily_time/")
    fun updateTime(@Header("Authorization") authHeader: String, @Body times: Times): Call<ResponseBody>
}

interface CreateTime {
    @POST("work_time/")
    fun createTime(@Header("Authorization") authHeader: String, @Body times: Times): Call<ResponseBody>
}

interface GetUser {
    @GET("user_info/")
    fun getUser(@Header("Authorization") authHeader: String): Call<User>
}