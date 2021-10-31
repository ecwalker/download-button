package com.udacity.detail

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ContentDetailBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.MainActivity
import com.udacity.R
import com.udacity.cancelNotifications
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import timber.log.Timber


class DetailActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManager
    //private lateinit var binding: ContentDetailBinding?

    private var downloadText: String? = null
    private var downloadStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        Timber.i("onCreate called")
        setContentView(R.layout.activity_detail)
        val binding: ContentDetailBinding? =
            DataBindingUtil.bind(findViewById(R.id.downloadInfo_motionLayout))
        //binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)


        setSupportActionBar(toolbar)

        //Create instance of notification manager
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        //Cancel current notification
        notificationManager.cancelNotifications()

        //Get download bundle from intent
        Timber.i("Intent is $intent")
        val bundle: Bundle? = intent.getBundleExtra("downloadBundle")
        Timber.i("Bundle is: $bundle")

        //Extract download status and text
        downloadText = bundle?.getString("Download URL text")
        downloadStatus = bundle?.getString("Download status")
        val downloadBundle = DownloadBundle(downloadText, downloadStatus)

        binding?.downloadBundle = downloadBundle


        ok_button.setOnClickListener {
            val contentIntent = Intent(applicationContext, MainActivity::class.java)
            startActivity(contentIntent)
        }
    }
}
