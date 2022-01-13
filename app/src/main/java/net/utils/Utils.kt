package net.utils

import android.content.Context
import es.dmoral.prefs.Prefs
import net.basicmodel.MyApplication

object Utils {
    fun getRequestData(context: Context): RequestBean {
        val istatus = Prefs.with(context).readBoolean("isFirst", true)
        val requestBean = RequestBean()
        requestBean.appId = MyApplication().getAppId()
        requestBean.appName = MyApplication().getAppName()
        requestBean.applink = Prefs.with(context).read("facebook", "AppLink is empty")
        requestBean.ref = Prefs.with(context).read("google", "Referrer is empty")
        requestBean.token = MyApplication().getToken()
        requestBean.istatus = istatus
        return requestBean
    }
}