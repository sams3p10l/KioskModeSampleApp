package com.sams3p10l.kioskmodesample

import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

class MultiTapDetector(view: View, callback: (Int, Boolean) -> Unit) {
    data class Event(var time: Long = 0, var posX: Float = 0f, var posY: Float = 0f) {
        fun copyFrom(motionEvent: MotionEvent) {
            time = motionEvent.eventTime
            posX = motionEvent.x
            posY = motionEvent.y
        }

        fun clear() {
            time = 0
        }
    }

    private var numberOfTaps = 0
    private val handler = Handler()

    private val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout().toLong()
    private val tapTimeout = ViewConfiguration.getTapTimeout().toLong()
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    private val viewConfig = ViewConfiguration.get(view.context)

    private var downEvent = Event()
    private var lastTapUpEvent = Event()

    init {
        view.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.pointerCount == 1)
                        downEvent.copyFrom(event)
                    else
                        downEvent.clear()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.eventTime - event.downTime < tapTimeout
                        && abs(event.x - downEvent.posX) > viewConfig.scaledTouchSlop
                        && abs(event.y - downEvent.posY) > viewConfig.scaledTouchSlop)
                        downEvent.clear()
                }
                MotionEvent.ACTION_UP -> {
                    val downEvent = this.downEvent
                    val lastTapUpEvent = this.lastTapUpEvent

                    if (downEvent.time > 0 && event.eventTime - event.downTime < longPressTimeout) {
                        if (lastTapUpEvent.time > 0
                            && event.eventTime - lastTapUpEvent.time < doubleTapTimeout
                            && abs(event.x - lastTapUpEvent.posX) < viewConfig.scaledDoubleTapSlop
                            && abs(event.y - lastTapUpEvent.posY) < viewConfig.scaledDoubleTapSlop) {
                            numberOfTaps++
                        } else {
                            numberOfTaps = 1
                        }

                        this.lastTapUpEvent.copyFrom(event)

                        val taps = numberOfTaps
                        handler.postDelayed({
                            callback(taps, taps == numberOfTaps)
                        }, doubleTapTimeout)
                    }
                }
            }
            true
        }
    }
}