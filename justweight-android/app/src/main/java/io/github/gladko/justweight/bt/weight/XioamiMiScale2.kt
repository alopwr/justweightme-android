package io.github.gladko.justweight.bt.weight

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.net.ParseException
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.github.gladko.justweight.bt.CustomBtScale
import io.github.gladko.justweight.bt.Selector
import io.github.gladko.justweight.db.Measurement
import io.github.gladko.justweight.db.MeasurementDoa
import io.github.gladko.justweight.db.User
import io.github.gladko.justweight.ui.main.MainActivity
import io.realm.Realm
import io.realm.RealmCollection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and

@SuppressLint("CheckResult")
class XioamiMiScale2(rxBleClient: RxBleClient, val sharedPreferences: SharedPreferences) :
    CustomBtScale(rxBleClient, sharedPreferences) {
    private lateinit var realm: Realm
    private lateinit var user: User
    private lateinit var measurementDoa: MeasurementDoa
    private lateinit var list: RealmCollection<Measurement>
    private lateinit var localList: ArrayList<Measurement>
    private var currentYear = 0
    private var currentMonth = 0
    private var userAge = 0
    private lateinit var sdf : SimpleDateFormat

    init {
        measurementDoa = MeasurementDoa()
        measurementDoa.email = sharedPreferences.getString(MainActivity.EMAIL,"")?: ""
        measurementDoa.password = sharedPreferences.getString(MainActivity.PASSWORD,"")?: ""
        measurementDoa.initApi()
        sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Poland"));
        currentYear = Calendar.getInstance().get(Calendar.YEAR)
        currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    }

    companion object {
        const val MEASUREMENT_HISTORY = "00002a2f-0000-3512-2118-0009af100700"
        const val NOTIFICATION = "00002a2f-0000-3512-2118-0009af100700"
        const val WEIGHT_CUSTOM_CONFIG = "00001542-0000-3512-2118-0009af100700"
        const val CURRENT_TIME = "00002a2b-0000-1000-8000-00805f9b34fb"
        const val UNIQUE_NUMBER = "UNIQUE_NUMBER"
    }

    override fun init(rxBleConnection: RxBleConnection) {
        realm = Realm.getDefaultInstance()
        user = realm.where(User::class.java).findFirst() ?: User()

        list = realm.where(Measurement::class.java).findAll()

        //setup scale units
        localList = ArrayList<Measurement>()
        val unit = byteArrayOf(
            0x06.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0.toByte()
        )
        rxBleConnection.writeCharacteristic(UUID.fromString(WEIGHT_CUSTOM_CONFIG), unit).subscribe({},{
            Timber.d("error scale units " + it.message)
        })

        var currentDateTime = Calendar.getInstance()
        var year = currentDateTime.get(Calendar.YEAR)
        var month = (currentDateTime.get(Calendar.MONTH) + 1).toByte()
        var day = currentDateTime.get(Calendar.DAY_OF_MONTH).toByte()
        var hour = currentDateTime.get(Calendar.HOUR_OF_DAY).toByte()
        var min = currentDateTime.get(Calendar.MINUTE).toByte()
        var sec = currentDateTime.get(Calendar.SECOND).toByte()
        var dateTimeByte = byteArrayOf((year).toByte(), (year shr 8).toByte(), month, day, hour, min, sec, 0x03, 0x00, 0x00)
        rxBleConnection.writeCharacteristic(UUID.fromString(CURRENT_TIME),dateTimeByte).subscribe({},{
            Timber.d("error date " + it.message)
        })
    }

    override fun getLastMeasurements(rxBleConnection: RxBleConnection) {
        super.getLastMeasurements(rxBleConnection)
        rxBleConnection.writeCharacteristic(UUID.fromString(MEASUREMENT_HISTORY),XioamiLib(User()).getByteFromInt(getRandomNumber()))
        setupNotification(rxBleConnection)
        invokeData(rxBleConnection)
    }


    override fun getAllMeasurements(rxBleConnection: RxBleConnection) {
        super.getAllMeasurements(rxBleConnection)
        setupNotification(rxBleConnection)
        invokeData(rxBleConnection)
    }

    private fun invokeData(rxBleConnection: RxBleConnection) {
        rxBleConnection.writeCharacteristic(
            UUID.fromString("00002a2f-0000-3512-2118-0009af100700"),
            byteArrayOf(0x02)
        ).subscribe({
        }, {
            Timber.d("error invokeData " + it.message)
        })
    }

    private fun setupNotification(rxBleConnection: RxBleConnection) {
        rxBleConnection.setupNotification(UUID.fromString(NOTIFICATION))
            .flatMap { it }
            .subscribe({
                if (it[0] == 0x03.toByte()) {
                    Timber.d("Scale stop byte received")
                    GlobalScope.launch {
                        var a = Selector().getOnlyValideData(localList)
                        sharedPreferences.edit().putFloat(MainActivity.LAST_WEIGHT, a.last().weight).putFloat(MainActivity.LAST_BF, a.last().bf_percent).commit()
                        MeasurementDoa().getInstance()
                            .saveMeasurements(a)
                    }
                    rxBleConnection.writeCharacteristic(
                        UUID.fromString(MEASUREMENT_HISTORY),
                        byteArrayOf(0x03)
                    )
                    val r = getRandomNumber()
                    val userIdentifier = byteArrayOf(
                        0x04.toByte(),
                        0xFF.toByte(),
                        0xFF.toByte(),
                        (r and 0xFF00 shr 8).toByte(),
                        (r and 0xFF shr 0).toByte()
                    )
                    rxBleConnection.writeCharacteristic(
                        UUID.fromString(NOTIFICATION),
                        userIdentifier
                    )

                }

                if (it.size === 26) {
                    val firstWeight = Arrays.copyOfRange(it, 0, 10)
                    val secondWeight = Arrays.copyOfRange(it, 10, 20)
                    parseBytes(firstWeight)
                    parseBytes(secondWeight)
                }

                if (it.size === 13) {
                    parseBytes(it)
                }

            }, {
                Timber.d("error setupNotification " + it.message)

            })
    }

    private fun getRandomNumber(): Int {
        val random = Random()
        var r = random.nextInt(65535 - 100 + 1) + 100
        sharedPreferences.edit().putInt(UNIQUE_NUMBER,r);
        return r
    }

    override fun parseBytes(data: ByteArray) {
        super.parseBytes(data)
        var measurement = Measurement()
        try {
            val ctrlByte0 = data[0]
            val ctrlByte1 = data[1]

            val isWeightRemoved = isBitSet(ctrlByte1, 7)
            val isDateInvalid = isBitSet(ctrlByte1, 6)
            val isStabilized = isBitSet(ctrlByte1, 5)
            val isLBSUnit = isBitSet(ctrlByte0, 0)
            val isCattyUnit = isBitSet(ctrlByte1, 6)
            val isImpedance = isBitSet(ctrlByte1, 1)

            if (isStabilized && !isWeightRemoved && !isDateInvalid) {

                val year =
                    (data[3] and 0xFF.toByte()).toInt() shl 8 or (data[2] and 0xFF.toByte()).toInt()
                val month = data[4].toInt()
                val day = data[5].toInt()
                val hours = data[6].toInt()
                val min = data[7].toInt()
                val sec = data[8].toInt()

                var impedance = 0.0f

                if (isImpedance) {
                    impedance =
                        ((data[10] and 0xFF.toByte()).toInt() shl 8 or ((data[9] and 0xFF.toByte()).toInt())).toFloat()
                }
                var yearTemp = currentYear
                if (month > currentMonth){
                    yearTemp--;
                }
                val date_string = "" + currentYear + "/" + month + "/" + day + "/" + hours + "/" + min
                val date_time = SimpleDateFormat("yyyy/MM/dd/HH/mm").parse(date_string)
                var xioamiLib = XioamiLib(user)
                var date = sdf.format(date_time)
                measurement.weight = xioamiLib.getWeight(data)
                measurement.bf_percent = xioamiLib.getBodyFat(measurement.weight, impedance)
                measurement.created_at = date.substring(0,date.lastIndexOf("+"))
                measurement.profile = user.id
                localList.add(measurement)


            }
        } catch (e: ParseException) {

        }

    }


    override fun saveMeasurements() {
        super.saveMeasurements()
    }

    override fun clear() {
        super.clear()
    }
}