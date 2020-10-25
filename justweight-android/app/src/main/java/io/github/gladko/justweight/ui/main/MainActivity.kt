package io.github.gladko.justweight.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.gladko.justweight.R
import io.github.gladko.justweight.db.MeasurementDoa
import io.github.gladko.justweight.db.Measurement
import io.github.gladko.justweight.ui.account.AccountActivity
import io.github.gladko.justweight.ui.btscan.BtScanActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_menu.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.singleTop
import org.jetbrains.anko.toast
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    companion object {
        const val MAC_ADDRESS = "MAC_ADDRESS"
        const val PREF_KEY = "dys"
        const val IF_FIRST_SCAN = "IF_FIRST_SCAN"
        const val IF_ACCOUNT_SET = "IF_ACCOUNT"
        const val IF_BT_SET = "IF_BT_SET"
        const val NO_OPEN_ACCOUNT = "NO_OPEN_ACCOUNT"
        const val EMAIL = "EMAIL"
        const val PASSWORD = "PASSWORD"
        const val LAST_WEIGHT = "LAST_WEIGHT"
        const val LAST_BF = "LAST_BF"
    }

    private lateinit var viewModel: MainViewModel
    private var isConnected = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        setupNav()
        sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        Realm.init(this)
        realm = Realm.getDefaultInstance();
        val intent = intent


        if (!intent.hasExtra(NO_OPEN_ACCOUNT)) {
            if (!sharedPreferences.getBoolean(IF_ACCOUNT_SET, false)) {
                startActivity(intentFor<AccountActivity>().singleTop())
            }
        }

        if (intent.hasExtra(NO_OPEN_ACCOUNT)) {
            if (!sharedPreferences.getBoolean(IF_BT_SET, false)) {
                startActivity(intentFor<BtScanActivity>())
            }
        }


        viewModel =
            ViewModelProviders.of(this as FragmentActivity).get(MainViewModel::class.java)
        if (sharedPreferences.getString(MAC_ADDRESS, "") != "") {
            viewModel.macAddress = sharedPreferences.getString(MAC_ADDRESS, "") ?: ""
            viewModel.connectDevice(sharedPreferences)

        }

        if (intent.hasExtra(MAC_ADDRESS)) {
            connectDevice(intent.getStringExtra(MAC_ADDRESS))
        }


        viewModel.isConnected.observe(this, Observer {
            isConnected = it
            invalidateOptionsMenu()
            if (it) {
                if (sharedPreferences.getBoolean(IF_FIRST_SCAN, true)) {
                    viewModel.getAllMeasurements()
                    longToast(getString(R.string.loong_time)).show()
                    sharedPreferences.edit().putBoolean(IF_FIRST_SCAN, false).apply()
                } else {
                }
            }
        })
        val list = realm.where(Measurement::class.java!!).findAll()

        list.addChangeListener { it ->
            Timber.d("aaaaa")
        }

        MeasurementDoa().getInstance().email = sharedPreferences.getString(EMAIL, "")?: ""
        MeasurementDoa().getInstance().password = sharedPreferences.getString(PASSWORD, "")?: ""

    }

    private fun connectDevice(macAddress: String) {
        viewModel.macAddress = macAddress
        sharedPreferences.edit()
            .putString(MAC_ADDRESS, macAddress)
            .putBoolean(IF_BT_SET, true)
            .apply()
        viewModel.connectDevice(sharedPreferences)
    }

    private fun setupNav() {
        val navView: BottomNavigationView = nav_view

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_score, R.id.navigation_goal, R.id.navigation_account
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(io.github.gladko.justweight.R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.bt -> {
                if (viewModel.isConnected.value ?: false) {
                    toast(getString(R.string.already_connected)).show()
                } else {
                    if (viewModel.macAddress == "") {
                        startActivity(Intent(this, BtScanActivity::class.java))
                    } else {
                        toast(getString(R.string.searching)).show()
                        viewModel.connectDevice(sharedPreferences)
                    }

                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (isConnected) {
            menu.findItem(R.id.bt)
                .setIcon(R.drawable.ic_bluetooth_white_24dp)
        } else {
            menu.findItem(R.id.bt)
                .setIcon(R.drawable.ic_bluetooth_disabled_white_24dp)
        }
        return super.onPrepareOptionsMenu(menu)
    }



}
