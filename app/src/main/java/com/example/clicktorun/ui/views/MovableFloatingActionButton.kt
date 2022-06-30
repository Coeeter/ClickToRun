package com.example.clicktorun.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.example.clicktorun.utils.CLICK_TOLERANCE
import kotlin.math.abs

class MovableFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attr, defStyleAttr), View.OnTouchListener {

    init {
        setOnTouchListener(this)
    }

    private var downRawX = 0f
    private var downRawY: Float = 0f
    private var dX = 0f
    private var dY: Float = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.isPressed = true
                downRawX = event.rawX
                downRawY = event.rawY
                dX = v.x - downRawX
                dY = v.y - downRawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                v.isPressed = false
                val viewParent = v.parent as View
                var newX = event.rawX + dX
                newX = v.marginLeft.toFloat().coerceAtLeast(newX)
                newX = (viewParent.width.toFloat() - v.width - v.marginRight).coerceAtMost(newX)
                var newY = event.rawY + dY
                newY = v.marginTop.toFloat().coerceAtLeast(newY)
                newY = (viewParent.height.toFloat() - v.width - v.marginBottom).coerceAtMost(newY)
                v.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()
                true
            }
            MotionEvent.ACTION_UP -> {
                v.isPressed = false
                val upDX = event.rawX - downRawX
                val upDY = event.rawY - downRawY
                if (abs(upDX) < CLICK_TOLERANCE && abs(upDY) < CLICK_TOLERANCE)
                    return performClick()
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

}
