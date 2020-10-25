package io.github.gladko.justweight.ui.btscan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.github.gladko.justweight.JustWeight

import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class BtScanListViewModel : ViewModel() {
    private var scanDisposable: Disposable? = null
    private val rxBleClient = JustWeight.rxBleClient
    var btDevices = MutableLiveData<ArrayList<BtScanObject>>()
    private var isScanning = false

    override fun onCleared() {
        super.onCleared()
        scanDisposable?.dispose()
        Timber.d("onCleared")
    }

    init {
        btDevices.postValue(arrayListOf<BtScanObject>())
    }
    fun startBtScanning() {
        if (isScanning) {
            scanDisposable?.dispose()
        } else {
           val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            val scanFilter = ScanFilter.Builder().build()

            scanDisposable = rxBleClient.scanBleDevices(scanSettings,scanFilter)
                .observeOn(Schedulers.io())
                .doOnSubscribe { } // TODO show searching animation
                .subscribe( { scanResult ->
                    run {
                        var list = btDevices.value ?: return@run
                        if(btDevices.value?.any{ scan -> scan.address == scanResult.bleDevice.bluetoothDevice.address } == false){
                            list.add(BtScanObject(scanResult.bleDevice.bluetoothDevice.name ?: "",scanResult.bleDevice.bluetoothDevice.address ?: "",scanResult.bleDevice.bluetoothDevice.type.toString() ?: ""))
                            btDevices.postValue(list)
                            Timber.d("list " + btDevices.value.toString())
                        }
                    }
                }, {
                        t: Throwable? ->  Timber.d("Throwable " + t?.message)
                }
                )
            isScanning = true
        }

    }

    fun stopBtScanning(){
        scanDisposable?.dispose()
        isScanning = false
    }


}


