package com.garbagecollection

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.garbagecollection.common.GCCommon.Companion.deviceId
import com.garbagecollection.common.GCCommon.Companion.notificationToken
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging


/**
 * Created by Mithilesh Kumar on 26/08/2021.
 */

class MainActivity : AppCompatActivity() {

    @SuppressLint("InflateParams", "HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler(Looper.getMainLooper()).postDelayed({

            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                notificationToken = task.result!!
                deviceId = Settings.Secure.getString(
                    applicationContext.contentResolver,
                    Settings.Secure.ANDROID_ID
                )

            })

            navigateToActivity()
        }, 3000)

    }

    fun navigateToActivity() {
        val intent = Intent(this@MainActivity, ContainerActivity::class.java)
        startActivity(intent)
        overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
        finish()
    }


}