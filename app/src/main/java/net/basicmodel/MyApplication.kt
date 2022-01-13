package net.basicmodel

import com.xxxxxxh.lib.BaseApplication

class MyApplication :BaseApplication(){
    override fun getAppId(): String {
        return "361"
    }

    override fun getAppName(): String {
        return "net.basicmodel"
    }

    override fun getUrl(): String {
        return "http://smallfun.xyz/worldweather361/"
    }

    override fun getAesPassword(): String {
        return "VPWaTtwYVPS1PeQP"
    }

    override fun getAesHex(): String {
        return "jQ4GbGckQ9G7ACZv"
    }

    override fun getToken(): String {
        return ""
    }
}