package io.github.gladko.justweight.bt

import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.disposables.Disposable
import android.R.id.edit
import android.preference.PreferenceManager
import android.content.SharedPreferences
import android.net.MacAddress
import com.polidea.rxandroidble2.RxBleConnection
import kotlin.random.Random


open class CustomBtScale(rxBleClient: RxBleClient, sharedPreferences: SharedPreferences){
    var disposable: Disposable? = null
    var macAddress: String = ""
    open fun getLastMeasurements(rxBleConnection: RxBleConnection){

    }
    open fun getAllMeasurements(rxBleConnection: RxBleConnection){

    }
    open fun init(rxBleConnection: RxBleConnection){

    }
    open fun parseBytes(data: ByteArray){

    }

    open fun isBitSet(value: Byte, bit: Int): Boolean {
        return value.toInt() and (1 shl bit) != 0
    }

    open fun saveMeasurements(){

    }
    open fun clear() {
        disposable?.dispose()
    }
}