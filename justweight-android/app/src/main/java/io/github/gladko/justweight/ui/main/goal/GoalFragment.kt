package io.github.gladko.justweight.ui.main.goal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import io.github.gladko.justweight.R
import io.github.gladko.justweight.db.BMIUtils
import io.github.gladko.justweight.db.User
import io.github.gladko.justweight.ui.main.MainActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_goal.*
import kotlin.math.roundToInt


class GoalFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_goal, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var pref = activity?.getSharedPreferences(MainActivity.PREF_KEY, Context.MODE_PRIVATE)
        var last_w = pref?.getFloat(MainActivity.LAST_WEIGHT, 0f)?: 0f
        var last_bf = pref?.getFloat(MainActivity.LAST_BF, 0f)?: 0f
        var user = Realm.getDefaultInstance().where(User::class.java).findFirst()?: User()
        var  bmi = BMIUtils.calculateBMI(last_w.toDouble(), user?.height?.toDouble()/100)
        if(bmi.second.toInt() == 0){
            bmi_card.visibility = View.GONE
        }
        bt_item_type1.text = bmi.first
        bmi_card.setCardBackgroundColor(BMIUtils.getCategoryIdentifier(bmi.second))
        var goal = last_w / user.goal_weight
        if(goal >= 1){
            percent_to_goal1.setText(getString(R.string.goal_get))
        }else{
            var a = (goal * 100).roundToInt()
            percent_to_goal1.setText(a.toString() + "%")
        }
        percent_to_goal2.setText("Cel to " + user.goal_weight + "kg")

        bt_item_mac.setText(last_bf.roundToInt().toString())

    }
}
