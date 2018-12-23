package pl.restaurant.restaurantmobile.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class User() {
    constructor(username : String, firstName : String, isActive : Boolean) : this(){
        this.username = username
        this.firstName = firstName
        this.isActive = isActive
    }

    @SerializedName("username")
    @Expose
    var username : String = ""


    @SerializedName("is_active")
    @Expose
    var isActive : Boolean = false


    @SerializedName("first_name")
    @Expose
    var firstName : String? = null

}