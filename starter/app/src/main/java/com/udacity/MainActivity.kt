package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var selectedUrl: String? = null
    private var selectedUrlText: String? = null
    private val downloadBundle = Bundle(2)

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        Timber.i("onCreate called")
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        //Create instance of notification manager
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        //Create notification channel
        createChannel(
            getString(R.string.download_notification_channel_id),
            getString(R.string.download_notification_channel_name)
        )

        custom_button.setOnClickListener {
            Timber.i("onClickListener called. Selected URL: $selectedUrl")
            if (selectedUrl != null) {
                it.custom_button.onDownloadStart()
                download(selectedUrl!!)
            } else {
                Toast.makeText(this, "Please select file to download", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Functions/objects for downloading files
     */

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Timber.i("Download completed. Extra download id: $id")
            //Construct bundle
            val status = checkStatus(id)
            downloadBundle.putString("Download status", status)
            downloadBundle.putString("Download URL text", selectedUrlText)
            Timber.i("Download bundle is: $downloadBundle")
            //Send notification
            Timber.i("Context is $context")
            Timber.i("Intent is: $intent")
            context?.let{
                notificationManager.sendNotification(it.getString(R.string.notification_description),
                    it, downloadBundle)
            }
        }
    }

    private fun download(URL: String) {
        Timber.i("download() called")
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val LOADAPP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val RETROFIT_URL =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }


    /**
     * Functions for download notification
     */
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // Change importance
                NotificationManager.IMPORTANCE_HIGH
            )
                // Disable badges for this channel
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download complete"

            notificationManager.createNotificationChannel(notificationChannel)
        }

    }

    private fun checkStatus(id: Long?): String {
        if (id == -1L) {
            return "Fail"
        } else {
            return "Success"
        }
    }


    /**
     * Function for monitoring radio group
     */

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.glide_radioButton ->
                    if (checked) {
                        Timber.i("Glide radio button checked")
                        selectedUrl = GLIDE_URL
                        selectedUrlText = getString(R.string.glide_radio_text)
                    }
                R.id.loadApp_radioButton ->
                    if (checked) {
                        Timber.i("LoadApp radio button checked")
                        selectedUrl = LOADAPP_URL
                        selectedUrlText = getString(R.string.loadApp_radio_text)
                    }
                R.id.retrofit_radioButton ->
                    if (checked) {
                        Timber.i("Retrofit radio button checked")
                        selectedUrl = RETROFIT_URL
                        selectedUrlText = getString(R.string.retrofit_radio_text)
                    }
            }
        }
    }

}
