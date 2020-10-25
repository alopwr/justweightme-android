package io.github.gladko.justweight.ui.main.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.gladko.justweight.R
import io.github.gladko.justweight.db.Measurement
import io.github.gladko.justweight.ui.main.MainActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_account_local.*


class SettingsFragmnet : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_account_local, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var pref = activity?.getSharedPreferences(MainActivity.PREF_KEY, Context.MODE_PRIVATE)

        button.setOnClickListener {
            var a = Realm.getDefaultInstance()
            var b = a.where(Measurement::class.java).findAll()
            a.beginTransaction()
            b.deleteAllFromRealm()
            a.commitTransaction()
        }
        button2.setOnClickListener {
            pref?.edit()?.putString(MainActivity.MAC_ADDRESS, "")?.apply()

        }
        button3.setOnClickListener {
            pref?.edit()?.putString(MainActivity.EMAIL, "")?.putString(MainActivity.PASSWORD, "")
                ?.putBoolean(MainActivity.IF_ACCOUNT_SET, false)?.apply()
        }
    }
}