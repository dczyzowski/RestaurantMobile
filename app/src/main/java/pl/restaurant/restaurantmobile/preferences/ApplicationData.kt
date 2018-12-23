package pl.restaurant.restaurantmobile.preferences

import android.content.Context
import android.content.SharedPreferences

class ApplicationData(preferenceName : String, context : Context){

    val sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)


    fun getUsers(){
        sharedPreferences.all
    }
}