package com.example.clicktorun.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.gms.maps.MapView

class CustomMap : MapView {

    constructor(
        context: Context,
    ) : super(context)

    constructor(
        context: Context,
        attrs: AttributeSet,
    ) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int,
    ) : super(context, attrs, defStyle)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(ev)
    }
}