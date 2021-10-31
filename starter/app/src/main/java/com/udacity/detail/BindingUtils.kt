package com.udacity.detail

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter

//TODO: Implement binding util to change colour of download status text depending on status

@BindingAdapter("setDownloadStatusColour")
fun setDownloadStatusColour(textView: TextView, status: String?) {
    //val context = textView.context
    if (status == "Success") {
        textView.setTextColor(Color.GREEN)
    } else {
        textView.setTextColor(Color.RED)
    }

}