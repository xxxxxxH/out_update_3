package com.xxxxxxh.lib

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.mylhyl.acp.Acp
import com.mylhyl.acp.AcpListener
import com.mylhyl.acp.AcpOptions
import com.zhouyou.http.EasyHttp
import es.dmoral.toasty.Toasty

abstract class BaseActivity : AppCompatActivity() {

    protected var appLink: String? = null
    protected var installReferrer: String? = null
    var msgCount = 0

    private val handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                msgCount++
                if (msgCount == 10) {
                    next()
                } else {
                    if (!TextUtils.isEmpty(appLink) && !TextUtils.isEmpty(installReferrer)) {
                        next()
                    } else {
                        sendEmptyMessageDelayed(1, 1000)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        Toasty.Config.getInstance().apply()
        Acp.getInstance(this).request(
            AcpOptions.Builder().setPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).build(), object : AcpListener {
                override fun onGranted() {
                    appLink = CommonUtils.getFacebookInfo(this@BaseActivity)
                    installReferrer = CommonUtils.getGoogleInfo(this@BaseActivity)
                    handler.sendEmptyMessage(1)
                }

                override fun onDenied(permissions: MutableList<String>?) {
                    finish()
                }
            })
    }

    abstract fun layoutId(): Int

    abstract fun next()
}