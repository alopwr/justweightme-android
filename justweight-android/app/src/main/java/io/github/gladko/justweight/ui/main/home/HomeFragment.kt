package io.github.gladko.justweight.ui.main.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import io.github.gladko.justweight.R
import io.github.gladko.justweight.db.Measurement
import io.github.gladko.justweight.ui.main.MainViewModel
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.support.v4.browse
import timber.log.Timber
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            val tempVals = LinkedList<Entry>()
            var realm = Realm.getDefaultInstance()
            var a = realm.where(Measurement::class.java).findAll()
        if (a.isNotEmpty()) {
            var b = arrayOf<Measurement>()
            var c = a.toList()
            if(c.size > 20){
                c = c.subList(0, 20)
            }

            c.forEachIndexed { it, it1 ->
                tempVals.add(Entry((it + 10).toFloat(),it1.weight))
            }
            Timber.d("realm size " + a.size)
            Timber.d("realm size1 " + c.size)

            Collections.sort(tempVals, EntryXComparator());
            var dataSet = LineDataSet(tempVals, getString(R.string.desc))
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.RED);
            chart.data = LineData(dataSet)
            chart.description.isEnabled = false;
            chart.invalidate()
        }
        button4.setOnClickListener {
           browse("https://justweight.me/dashboard")
        }

    }
}
