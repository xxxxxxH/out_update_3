package net.basicmodel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.xxxxxxh.lib.CommonUtils
import com.xxxxxxh.lib.MessageEvent
import net.utils.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        CommonUtils.update(JSON.toJSONString(Utils.getRequestData(this)))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e:MessageEvent){
        val msg = e.getMessage()
        when(msg[0]){
            "onError" -> {

            }
            "onSuccess" -> {

            }
            "perOk" -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}