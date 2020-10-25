package io.github.gladko.justweight.bt

import io.github.gladko.justweight.db.Measurement
import timber.log.Timber

class Selector() {
    fun  getOnlyValideData(list: ArrayList<Measurement>): ArrayList<Measurement>{
        var temp = list.reversed()
        var temp1 = ArrayList<Measurement>()
        Timber.d("getOnlyValideData "  + list.size)
        var last = temp.first().weight
        temp.forEach{

            if (it.weight > last){
                if (last / it.weight >= 0.80f){
                    temp1.add(it)
                }

            }else{
                if (it.weight  / last >= 0.80f){
                    temp1.add(it)
                }
            }
            last = temp1.last().weight
        }
        temp1.reverse()
        Timber.d("getOnlyValideData " + temp1.size)
        return temp1

    }
}