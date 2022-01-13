package net.basicmodel

import android.os.Environment
import android.util.Log
import com.xxxxxxh.lib.BaseApplication
import net.utils.Utils
import java.io.File
import java.util.*

class MyApplication : BaseApplication() {
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
        var token: String? = null
        if (!File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "a.testupdate.txt"
            ).exists()
        ) {
            token = UUID.randomUUID().toString()
            Utils.saveFile(token)
        } else {
            token =
                Utils.readrFile(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + "a.testupdate.txt"
                )
        }
        Log.i("xxxxxxH","token = $token")
        return token
    }
}