package io.github.gladko.justweight.ui.btscan

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import io.github.gladko.justweight.JustWeight
import io.github.gladko.justweight.ui.main.MainActivity
import io.github.gladko.justweight.R
import io.github.gladko.justweight.ui.main.MainActivity.Companion.PREF_KEY
import kotlinx.android.synthetic.main.activity_bluetooth_scan.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop
import org.jetbrains.anko.toast
import timber.log.Timber


class BtScanActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_ENABLE_LOCATION = 2
        const val REQUEST_BT_PERMISSION = 3
        const val IF_WITHOUT_NAME_MENU = "IF_WITHOUT_NAME_MENU"

    }
    private lateinit var viewModel: BtScanListViewModel
    private lateinit var adapter: BtScanAdapter
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_scan)
        sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        viewModel =
            ViewModelProviders.of(this as FragmentActivity).get(BtScanListViewModel::class.java)
        viewModel.btDevices.observe(this, Observer {
            it?.let {
                updateList()
            }

        })

        sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        adapter = BtScanAdapter(object : OnClickListener {
            override fun onConnectMeClick(macAddress: String) {
                startActivity(intentFor<MainActivity>(MainActivity.MAC_ADDRESS to macAddress).singleTop())
                sharedPreferences.edit().putString(MainActivity.MAC_ADDRESS,macAddress).apply()
                finish()
            }

        })
        if (hasPermissions()) {
            checkBluetooth()
        }
        bt_scale_rv.adapter = adapter
        swipeContainer.setOnRefreshListener(object: SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                checkBluetooth()
            }
        })
        swipeContainer.setColorSchemeResources(
                R.color.secondaryColor,
                R.color.primaryColor,
                        R.color.primaryDarkColor)
    }


    private fun checkBluetooth() {
        if(BluetoothAdapter.getDefaultAdapter() == null){
            toast(getString(R.string.need_bt)).show()
        }else {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
                checkLocation()
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                val REQUEST_ENABLE_BT = 1
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    private fun initBluetoothScan() {
        hasPermissions()
        if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
            viewModel.startBtScanning()
            GlobalScope.launch(context = Dispatchers.Main) {
                delay(5000)
                if (swipeContainer != null){
                    swipeContainer.isRefreshing = false
                }
            }

        } else {
            checkBluetooth()
        }

    }


    private fun hasPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_BT_PERMISSION
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_BT_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    checkBluetooth()
                } else {
                    hasPermissions()
                }
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(io.github.gladko.justweight.R.menu.bt_scan_menu, menu)
        menu.getItem(0).isChecked =  sharedPreferences.getBoolean(IF_WITHOUT_NAME_MENU, false)
        updateList()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.if_without_name -> {
                sharedPreferences
                        .edit()
                        .putBoolean(IF_WITHOUT_NAME_MENU, !item.isChecked)
                        .apply()
                item.isChecked = !item.isChecked
                updateList()
                return true
            }
            R.id.refresh -> {
                checkBluetooth()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateList(){
        var list = viewModel.btDevices.value
        if(sharedPreferences.getBoolean(IF_WITHOUT_NAME_MENU, false)){
            adapter.submitList(list)
            adapter.notifyDataSetChanged()
        }else{
            GlobalScope.launch(context = Dispatchers.IO) {
                var listF = list?.filter{it.name != ""}
                runOnUiThread {
                    if (adapter != null){
                        adapter.submitList(listF)
                        adapter.notifyDataSetChanged()

                    }
                }
            }

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    checkLocation()
                } else {
                    checkBluetooth()
                }
            }
            REQUEST_ENABLE_LOCATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    initBluetoothScan()
                } else {
                    checkLocation()
                }
            }
        }
    }

    //required for ble
    private fun checkLocation() {
        val provider = Settings.Secure.getString(
            getContentResolver(),
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        if (provider != "") {
            //location enabled
            initBluetoothScan()
        } else {
            val mLocationRequest = LocationRequest().apply {
                priority = LocationRequest.PRIORITY_LOW_POWER
                interval = (600 * 6000).toLong()  // 60 min
                fastestInterval = (600 * 6000).toLong()  // 60 min
            }
            LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(
                    LocationSettingsRequest.Builder().addLocationRequest(
                        mLocationRequest
                    ).setNeedBle(true).build()
                )
                .addOnCompleteListener {
                    try {
                        it.getResult(ApiException::class.java)
                        Timber.d("success " + it.isSuccessful)
                        initBluetoothScan()

                    } catch (exception: ApiException) {
                        when (exception.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                                try {
                                    val resolvable = exception as ResolvableApiException;
                                    resolvable.startResolutionForResult(
                                        this,
                                        REQUEST_ENABLE_LOCATION
                                    )
                                } catch (e: IntentSender.SendIntentException) {
                                } catch (e: ClassCastException) {
                                }
                        }
                    }

                }
        }
    }


}




