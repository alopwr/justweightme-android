package io.github.gladko.justweight

import android.app.Application
import android.content.pm.ApplicationInfo
import com.facebook.stetho.Stetho
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import timber.log.Timber
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import javax.xml.datatype.DatatypeConstants.SECONDS
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import android.text.TextUtils
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import io.github.gladko.justweight.api.AuthenticationInterceptor
import okhttp3.Credentials
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.appcompat.app.AppCompatDelegate
import io.realm.Realm
import javax.crypto.Cipher


class JustWeight : Application() {
    companion object {
        lateinit var rxBleClient: RxBleClient
        lateinit var retrofit: Retrofit


        var logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        private val TIMEOUT_SECONDS = 60L
        private val httpClient = OkHttpClient.Builder().addInterceptor(logging)
        private val builder = Retrofit.Builder()
            .baseUrl("https://justweight.me/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

        fun <S> createService(serviceClass: Class<S>): S {
            return createService(serviceClass, null, null)
        }

        fun <S> createService(
            serviceClass: Class<S>, username: String?, password: String?
        ): S {
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                val authToken = Credentials.basic(username, password)
                return createService(serviceClass, authToken)
            }

            return createService(serviceClass, null)
        }



        fun <S> createService(
             serviceClass: Class<S>, authToken: String?
        ): S {
            if (!TextUtils.isEmpty(authToken)) {
                val interceptor = AuthenticationInterceptor(authToken?: "")

                if (!httpClient.interceptors().contains(interceptor)) {
                    httpClient.addInterceptor(interceptor)

                    builder.client(httpClient.build())
                    retrofit = builder.build()
                }
            }

            return retrofit.create(serviceClass)
        }

    }

    override fun onCreate() {
        super.onCreate()

//        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
//            Timber.plant(Timber.DebugTree())
//            Stetho.initializeWithDefaults(this);
//            RxBleClient.updateLogOptions(
//                LogOptions.Builder()
//                    .setLogLevel(LogConstants.VERBOSE)
//                    .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
//                    .setUuidsLogSetting(LogConstants.UUIDS_FULL)
//                    .setShouldLogAttributeValues(true)
//                    .build()
//            )
//        }

        rxBleClient = RxBleClient.create(this)

        retrofit = Retrofit.Builder()
            .baseUrl("https://justweight.me/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        Realm.init(this);


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

}