package com.garbagecollection


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.garbagecollection.common.NotificationX
import com.garbagecollection.viewUI.customers_list.CustomerListFragment
import com.garbagecollection.viewUI.map.MapContainerFragment
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.e("onMessageReceived", "$p0")
    }

    companion object {
        var mNotifyCurrentFrag: Fragment? = null
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e("onMessageReceived", "$remoteMessage")
        if (remoteMessage.data.isNotEmpty()) {
            val params = remoteMessage.data
            val obj = JSONObject(params as Map<*, *>)
            val payload = Gson().fromJson(obj.toString(), NotificationX::class.java)
            if (mNotifyCurrentFrag is CustomerListFragment) {
                (mNotifyCurrentFrag as CustomerListFragment).updateFrag(payload)
            } else if (mNotifyCurrentFrag is MapContainerFragment) {
                (mNotifyCurrentFrag as MapContainerFragment).updateFrag(payload)
            }
            getFirebaseMessage(payload?.title ?: "", payload?.notes ?: "")
        }

    }

    private fun getFirebaseMessage(title: String?, body: String?) {
        val notificationBuilder: NotificationCompat.Builder?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = NotificationCompat.Builder(this, packageName)
                .setSmallIcon(R.drawable.notification1)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setOngoing(false)
                .setChannelId(packageName)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationManager.IMPORTANCE_MAX)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    packageName,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
        } else {
            notificationBuilder = NotificationCompat.Builder(this, packageName)
                .setSmallIcon(R.drawable.notification1)
                .setContentTitle(body)
                .setAutoCancel(true)
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_ALL)

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder.setSmallIcon(R.drawable.notification1)
        } else {
            notificationBuilder.setSmallIcon(R.drawable.notification1)
        }
        val notificationManager = NotificationManagerCompat.from(this)
        notificationBuilder.priority = Notification.PRIORITY_MAX

        notificationManager.notify(0, notificationBuilder.build())

    }
}