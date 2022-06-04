package com.example.clicktorun.utils

import android.app.Activity
import android.content.Intent
import com.example.clicktorun.R

fun Activity.startActivityWithAnimation(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.darken)
}

fun Activity.endActivityWithAnimation() {
    overridePendingTransition(R.anim.brighten, R.anim.slide_out_right)
}
