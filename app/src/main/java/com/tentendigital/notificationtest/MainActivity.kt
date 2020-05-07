package com.tentendigital.notificationtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.tentendigital.notificationtest.notif.NotificationHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createNotificationChannel(this,
            NotificationManagerCompat.IMPORTANCE_HIGH, true,
            getString(R.string.app_name), "TerasDakwah Notification.")

        NotificationHelper.scheduleAlarmsForReminder(this)
    }
}
