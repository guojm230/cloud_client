package com.example.cloud.components

import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.sqrt

private const val DRAG_DISTANCE = 30
private const val SCROLL_DISTANCE = 10

private const val STATE_UNSET = 0
private const val STATE_DRAG = 1
private const val STATE_LONG_PRESS = 2

const val TAG = "com.example.cloud.components.GestureDetector"

/**
 * 用来区分点击、长按、拖动事件
 * 点击: 短时间内触发ACTION_DOWN和ACTION_UP
 * 长按：ACTION_DOWN和ACTION_UP的时间较长,且位移距离较短
 * 开始拖动：ACTION_MOVE和ACTION_UP的时间较长,且位移距离较长
 */
class GestureDetector: View.OnTouchListener {
    private var startPoint: Point = Point(0,0)
    private var startPressTime = 0L
    private var state = STATE_UNSET

    var onDragListener: ((View)->Unit)? = null

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouch: action_down")
                startPressTime = System.currentTimeMillis()
                startPoint = Point(event.rawX.toInt(),event.rawY.toInt())
            }
            MotionEvent.ACTION_MOVE ->{
                val distance = calculateInstance(Point(event.rawX.toInt(),event.rawY.toInt()),startPoint)
                Log.d(TAG, "onTouch: action_move $distance")
                val interval = System.currentTimeMillis() - startPressTime
                if (state == STATE_UNSET && interval >= ViewConfiguration.getLongPressTimeout()){
                    if (distance >= DRAG_DISTANCE){
                        Log.d(TAG, "onTouch: onDrag")
                        state = STATE_DRAG
                        onDragListener?.invoke(v)
                    } else {
                        state = STATE_LONG_PRESS
                        Log.d(TAG, "onTouch: onLongPress")
                        v.performLongClick()
                    }
                }
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL -> {
                if (state == STATE_UNSET){
                    val interval = System.currentTimeMillis() - startPressTime
                    val distance = calculateInstance(Point(event.rawX.toInt(),event.rawY.toInt()),startPoint)
                    if (interval < ViewConfiguration.getLongPressTimeout()
                        && distance <= SCROLL_DISTANCE
                    ){
                        Log.d(TAG, "onTouch: onCLick")
                        v.performClick()
                    }
                }
                state = STATE_UNSET
            }
        }
        return true
    }

    private fun calculateInstance(p1: Point, p2: Point): Int{
        return sqrt(
            (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y).toFloat()
        ).toInt()
    }
}