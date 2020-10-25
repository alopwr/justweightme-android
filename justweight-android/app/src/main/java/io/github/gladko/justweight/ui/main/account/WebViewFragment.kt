package io.github.gladko.justweight.ui.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import io.github.gladko.justweight.R
import kotlinx.android.synthetic.main.fragment_account_webview.*
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber


class WebViewFragment:  Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_account_webview, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webview.webViewClient  = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                if(!url.contains("mobile=1")){
                    if (url.contains("?")){
                        view.loadUrl("$url&mobile=1")
                    }else{
                        view.loadUrl("$url?mobile=1")

                    }
                }
                Timber.d("onPageFinished " + url)
            }
        }
        val webSetting = webview.getSettings()

        webSetting.setJavaScriptEnabled(true)
        webSetting.displayZoomControls = true
        webview.loadUrl("https://justweight.me/dashboard/settings?mobile=1")
        toast(getString(R.string.web_is_loading)).show()
    }
}
