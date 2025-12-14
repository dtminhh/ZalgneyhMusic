package com.example.zalgneyhmusic.ui.extension

import android.os.SystemClock
import android.view.View

/**
 * Extension function block double click/spam click
 * @param debounceTime between 2 click (Default 2000ms)
 * @param action
 */
fun View.setOnSingleClickListener(debounceTime: Long = 2000L, action: (View) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) {
                return
            }
            lastClickTime = SystemClock.elapsedRealtime()
            action(v)
        }
    })
}