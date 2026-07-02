package com.huiyi.v4.floating

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.huiyi.v4.MainActivity
import com.huiyi.v4.runtime.HuiyiRuntime

class FloatingBubbleService : Service() {
    private var controller: FloatingBubbleController? = null

    override fun onCreate() {
        super.onCreate()
        controller = FloatingBubbleController(this) {
            HuiyiRuntime.get(this).runNextSentence()
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
        controller?.show()
    }

    override fun onDestroy() {
        controller?.hide()
        controller = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
