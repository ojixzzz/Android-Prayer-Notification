/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.tentendigital.notificationtest.notif

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tentendigital.notificationtest.MainActivity
import com.tentendigital.notificationtest.R
import java.util.*

object NotificationHelper {

  /**
   * Sets up the notification channels for API 26+.
   * Note: This uses package name + channel name to create unique channelId's.
   *
   * @param context     application context
   * @param importance  importance level for the notificaiton channel
   * @param showBadge   whether the channel should have a notification badge
   * @param name        name for the notification channel
   * @param description description for the notification channel
   */
  fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean, name: String, description: String) {

    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

      val channelId = "${context.packageName}-$name"
      val channel = NotificationChannel(channelId, name, importance)
      channel.description = description
      channel.setShowBadge(showBadge)

      // Register the channel with the system
      val notificationManager = context.getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }
  }

  /**
   * Helps issue the default application channels (package name + app name) notifications.
   * Note: this shows the use of [NotificationCompat.BigTextStyle] for expanded notifications.
   *
   * @param context    current application context
   * @param title      title for the notification
   * @param message    content text for the notification when it's not expanded
   * @param bigText    long form text for the expanded notification
   * @param autoCancel `true` or `false` for auto cancelling a notification.
   * if this is true, a [PendingIntent] is attached to the notification to
   * open the application.
   */
  fun createSampleDataNotification(context: Context, title: String, message: String,
                                   bigText: String, autoCancel: Boolean) {

    val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
    val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
      setSmallIcon(R.drawable.ic_launcher_foreground)
      setContentTitle(title)
      setContentText(message)
      setAutoCancel(autoCancel)
      setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
      priority = NotificationCompat.PRIORITY_DEFAULT
      setAutoCancel(autoCancel)

      val intent = Intent(context, MainActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
      setContentIntent(pendingIntent)
    }

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(1001, notificationBuilder.build())
  }


  private fun scheduleAlarm(hour: Int, minute: Int, alarmIntent: PendingIntent?, alarmMgr: AlarmManager) {
    // Set up the time to schedule the alarm
    val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
    datetimeToAlarm.timeInMillis = System.currentTimeMillis()
    datetimeToAlarm.set(Calendar.HOUR_OF_DAY, hour)
    datetimeToAlarm.set(Calendar.MINUTE, minute)
    datetimeToAlarm.set(Calendar.SECOND, 0)
    datetimeToAlarm.set(Calendar.MILLISECOND, 0)

    Log.d("101010", datetimeToAlarm.time.toString())
    val today = Calendar.getInstance(Locale.getDefault())
    if (datetimeToAlarm.timeInMillis <= today.timeInMillis) {
      datetimeToAlarm.add(Calendar.DATE, 1)
      Log.d("101010", "ROOL " + datetimeToAlarm.time.toString())
    }

    //alarmMgr.setRepeating(
    //    AlarmManager.RTC_WAKEUP,
    //    datetimeToAlarm.timeInMillis, (1000 * 60 * 60 * 24 * 7).toLong(), alarmIntent)

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
          alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, datetimeToAlarm.timeInMillis, alarmIntent)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
          alarmMgr.setExact(AlarmManager.RTC_WAKEUP, datetimeToAlarm.timeInMillis, alarmIntent)
        }
        else -> {
          alarmMgr.set(AlarmManager.RTC_WAKEUP, datetimeToAlarm.timeInMillis, alarmIntent)
        }
    }

  }

  private fun createPendingIntent(context: Context, judul: String, deskripsi: String): PendingIntent? {
    // create the intent using a unique type
    val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
      action = "com.terasdakwah.notification.action.SHOLAT"
      type = "$judul-terasdakwah-sholat"
      putExtra("judul", judul)
      putExtra("deskripsi", deskripsi)
    }

    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  fun scheduleAlarmsForReminder(context: Context) {
    // get the AlarmManager reference
    val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val subuhjam = 4
    val subuhmenit = 30

    val dzuhurjam = 12
    val dzuhurmenit = 0

    val asharjam = 15
    val asharmenit = 0

    val maghribjam = 17
    val maghribmenit = 30

    val isyajam = 19
    val isyamenit = 0

    val alarmIntent = createPendingIntent(context, "Subuh", "Waktu sholat Subuh")
    scheduleAlarm(subuhjam, subuhmenit, alarmIntent, alarmMgr)

    val alarmIntent1 = createPendingIntent(context, "Dzuhur", "Waktu sholat Dzhuhur")
    scheduleAlarm(dzuhurjam, dzuhurmenit, alarmIntent1, alarmMgr)

    val alarmIntent2 = createPendingIntent(context, "Ashar", "Waktu sholat Ashar")
    scheduleAlarm(asharjam, asharmenit, alarmIntent2, alarmMgr)

    val alarmIntent3 = createPendingIntent(context, "Maghrib", "Waktu sholat Maghrib")
    scheduleAlarm(maghribjam, maghribmenit, alarmIntent3, alarmMgr)

    val alarmIntent4 = createPendingIntent(context, "Isya", "Waktu sholat Isya")
    scheduleAlarm(isyajam, isyamenit, alarmIntent4, alarmMgr)
  }

}