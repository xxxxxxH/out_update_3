package com.xxxxxxh.lib

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.ImageView
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.facebook.applinks.AppLinkData
import com.yarolegovich.lovelydialog.LovelyCustomDialog
import com.zhouyou.http.EasyHttp
import com.zhouyou.http.callback.SimpleCallBack
import com.zhouyou.http.exception.ApiException
import es.dmoral.prefs.Prefs
import org.greenrobot.eventbus.EventBus

object CommonUtils {
    fun getFacebookInfo(context: Context): String {
        var appLink = ""
        if (TextUtils.isEmpty(Prefs.with(context).read("facebook"))) {
            AppLinkData.fetchDeferredAppLinkData(context) {
                appLink = it?.targetUri?.toString() ?: "Applink is empty"
                if (!TextUtils.equals(appLink, "Applink is empty")) {
                    Prefs.with(context).write("facebook", appLink)
                }
            }
        } else {
            appLink = Prefs.with(context).read("facebook")
        }

        return appLink
    }

    fun getGoogleInfo(context: Context): String {
        var installReferrer = ""
        if (TextUtils.isEmpty(Prefs.with(context).read("google"))) {
            val ref = InstallReferrerClient.newBuilder(context).build()
            ref.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    installReferrer =
                        if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                            ref.installReferrer.installReferrer
                        } else {
                            "Referrer is empty"
                        }
                    if (!TextUtils.equals(installReferrer, "Referrer is empty")) {
                        Prefs.with(context).write("google", installReferrer)
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    installReferrer = "Referrer is empty"
                }

            })
        } else {
            installReferrer = Prefs.with(context).read("google")
        }

        return installReferrer
    }

    fun update(requestData: String) {
        val url = "http://smallfun.xyz/worldweather361/weather1.php"
        EasyHttp.post(url)
            .params("data", requestData)
            .execute<String>(object : SimpleCallBack<String>() {
                override fun onError(e: ApiException?) {
                    EventBus.getDefault().post(MessageEvent("onError"))
                }

                override fun onSuccess(t: String?) {
                    EventBus.getDefault().post(MessageEvent("onSuccess", t!!))
                }
            })
    }

    fun createPermissionDlg(context: Context, imgUrl: String,imgId:Int) {
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_permission, null)
        val imageView = v.findViewById<ImageView>(R.id.img)
        imageView.setImageResource(imgId)
        LovelyCustomDialog(context)
            .setTitle("Permission")
            .setView(v)
            .setListener(R.id.tv_ok, true) { EventBus.getDefault().post(MessageEvent("perOk")) }
            .show()
    }
}