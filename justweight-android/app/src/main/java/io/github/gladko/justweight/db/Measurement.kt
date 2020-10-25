package io.github.gladko.justweight.db

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class Measurement(
    @SerializedName("id") var id_ : Int = 0,
    @SerializedName("profile") var profile : Int = 0,
    @SerializedName("weight") var weight : Float = 0F,
    @SerializedName("bf_percent") var bf_percent : Float = 0F,
    @SerializedName("created_at") var created_at : String = ""
): RealmObject()