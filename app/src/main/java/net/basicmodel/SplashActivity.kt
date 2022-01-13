package net.basicmodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xxxxxxh.lib.BaseActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {


    override fun layoutId(): Int {
        return R.layout.activity_splash
    }

    override fun next() {
        startActivity(Intent(this,MainActivity::class.java))
    }

    override fun showLoading() {
        rotateloading.start()
    }

    override fun dismissLoading() {
        rotateloading.stop()
    }
}