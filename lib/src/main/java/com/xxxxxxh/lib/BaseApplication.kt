package com.xxxxxxh.lib

import android.app.Application
import com.zhouyou.http.EasyHttp

abstract class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        EasyHttp.init(this)
    }

    abstract fun getAppId(): String
    abstract fun getAppName(): String
    abstract fun getUrl(): String
    abstract fun getAesPassword(): String
    abstract fun getAesHex(): String
    abstract fun getToken(): String
}