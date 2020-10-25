package io.github.gladko.justweight.ui.main

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.github.gladko.justweight.JustWeight
import io.github.gladko.justweight.bt.BtScaleSelector
import io.github.gladko.justweight.bt.CustomBtScale
import io.github.gladko.justweight.bt.weight.XioamiMiScale2
import io.github.gladko.justweight.db.MeasurementDoa
import io.github.gladko.justweight.db.User
import io.reactivex.disposables.Disposable
import io.realm.Realm
import timber.log.Timber




class MainViewModel : ViewModel() {

    private var disposable: Disposable? = null
    private val rxBleClient = JustWeight.rxBleClient
    private lateinit var device: RxBleDevice
    private lateinit var scale: CustomBtScale
    private lateinit var rxBleConnection: RxBleConnection
    var isConnected = MutableLiveData<Boolean>()
    var macAddress: String = ""
    lateinit var user: User

    init {
        var realm = Realm.getDefaultInstance()
        user = realm.where(User::class.java).findFirst()?: User()
    }

    fun connectDevice(sharedPreferences: SharedPreferences) {
        if (isConnected.value?: false){
            disposable?.dispose()
        }
        device = rxBleClient.getBleDevice(macAddress)

        device.establishConnection(false).subscribe({
            rxBleConnection = it
            BtScaleSelector(rxBleClient,sharedPreferences).findWeight(device.name.toString())?.let {
                scale = it
                scale.macAddress = macAddress
            }
            scale.init(it)
            isConnected.postValue(true)
        },{
            Timber.d("popsuło sie " + it.message)
        }).let { disposable -> disposable }


    }

    fun getAllMeasurements(){
        if (::scale.isInitialized){
            scale.getAllMeasurements(rxBleConnection)
        }
    }
    fun getLastMeasurements(){
        if (::scale.isInitialized){
            scale.getLastMeasurements(rxBleConnection)
        }
    }
    private fun isBitSet(value: Byte, bit: Int): Boolean {
        return value.toInt() and (1 shl bit) != 0
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
        MeasurementDoa().getInstance().clear()
    }
}