package io.github.gladko.justweight.db

import io.github.gladko.justweight.JustWeight
import io.github.gladko.justweight.api.APIService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import timber.log.Timber

class MeasurementDoa(){

    private var instance: MeasurementDoa? = null
    private lateinit var realm: Realm
    private lateinit var service: APIService
    var email: String = ""
    var password: String = ""

    private var disposable: CompositeDisposable = CompositeDisposable()

    init {
        realm = Realm.getDefaultInstance()
    }

    fun initApi(){
        service = JustWeight.createService(
            APIService::class.java,
            email,
            password
        )
    }

    fun getInstance(): MeasurementDoa {
        if (instance == null) {
            instance = MeasurementDoa()

        }

        return instance as MeasurementDoa
    }

    fun saveMeasurement(measurement: Measurement){
        Timber.d("api start  " + measurement.created_at)

        disposable.add(service.addMeasurements(measurement).subscribe({
            realm.beginTransaction()
            realm.copyToRealm(measurement)
            realm.commitTransaction()
            Timber.d("api ok "  + it.id_)

        },{
            Timber.d("api error "  + it.message)
        }))
    }
    suspend fun saveMeasurements(list: List<Measurement>){
        initApi()
        list.forEach {
            saveMeasurement(it)
        }

        Timber.d("saveMeasurements " + list.size)
    }

    fun clear(){
        disposable.clear()
    }

}