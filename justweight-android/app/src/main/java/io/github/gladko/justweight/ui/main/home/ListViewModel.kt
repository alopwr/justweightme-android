package io.github.gladko.justweight.ui.main.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.gladko.justweight.db.Measurement
import io.realm.Realm

class ListViewModel: ViewModel(){
    private lateinit var realm: Realm
    var list = MutableLiveData<Array<Measurement>>()
    init {
        realm = Realm.getDefaultInstance()

    }

    fun start(){
        var list1 = arrayOf<Measurement>()
        realm.where(Measurement::class.java).findAll().toArray(list1)
        list.postValue(list1)
    }
}
