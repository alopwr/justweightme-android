package io.github.gladko.justweight.bt

import android.content.SharedPreferences
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice

import io.github.gladko.justweight.bt.weight.XioamiMiScale2

class BtScaleSelector(val rxBleClient: RxBleClient, val sharedPreferences: SharedPreferences){
    fun findWeight(name: String): CustomBtScale? {

        if(name.startsWith("MIBCS") || name.startsWith("MIBFS")){
            return XioamiMiScale2(rxBleClient, sharedPreferences)
        }else{
            return null
        }
    }

}