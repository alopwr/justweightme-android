package io.github.gladko.justweight.db

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class User (
    @SerializedName("id") var id : Int = 0,
    @SerializedName("height") var height : Int = 0,
    @SerializedName("date_of_birth") var date_of_birth : String = "",
    @SerializedName("sex") var sex : String = "",
    @SerializedName("goal_weight") var goal_weight : Int = 0
): RealmObject()