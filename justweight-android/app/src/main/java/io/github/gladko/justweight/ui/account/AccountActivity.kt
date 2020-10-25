package io.github.gladko.justweight.ui.account

import android.content.Context
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import io.github.gladko.justweight.JustWeight
import io.github.gladko.justweight.R
import io.github.gladko.justweight.api.APIService
import io.github.gladko.justweight.db.User
import io.github.gladko.justweight.ui.main.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_account.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop
import timber.log.Timber

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class AccountActivity : AppCompatActivity() {
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(io.github.gladko.justweight.R.layout.activity_account)
        var sharedPreferences = getSharedPreferences(MainActivity.PREF_KEY, Context.MODE_PRIVATE)
        register.setOnClickListener {
            browse("https://justweight.me/accounts/register/")
        }

        login.setOnClickListener {
            val loginService = JustWeight.createService(
                APIService::class.java,
                login_input.text.toString(),
                password_input.text.toString()
            )

            disposable = loginService.getProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.isNotEmpty()) {
                            val realm = Realm.getDefaultInstance()
                            realm.beginTransaction()
                            val rows = realm.where(User::class.java).findAll()
                            rows.deleteAllFromRealm()
                            realm.copyToRealm(it.get(0))
                            realm.commitTransaction()

                            sharedPreferences.edit()
                                .putString(MainActivity.EMAIL, login_input.text.toString())
                                .putString(MainActivity.PASSWORD,password_input.text.toString())
                                .putBoolean(MainActivity.IF_ACCOUNT_SET, true).apply()
                            startActivity(intentFor<MainActivity>(MainActivity.NO_OPEN_ACCOUNT to "aaa"))

                        }
                    }, {
                        Timber.d("pass " + it.message)

                        account_activity.snackbar(
                            getString(R.string.not_authorized),
                            getString(R.string.reset_password)
                        ) { browse("https://justweight.me/accounts/password_reset/") }
                    }
                )
        }
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

}
