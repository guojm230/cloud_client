package com.example.cloud.page

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import com.example.cloud.R

/**
 * 通过onTouch来实现拖拽事件
 */
class DragDemoFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_drag_demo, container, false)
        root.findViewById<LinearLayoutCompat>(R.id.list_view).children.forEach {
            initDragEvent(it)
        }
        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initDragEvent(view: View) {
        view.setOnTouchListener(object : OnTouchListener {

            var startDrag: Boolean = false
            var shadowView: View? = null

            var containerWeight = 0
            var containerHeight = 0

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        if (!startDrag) {
                            val decorView = requireActivity().window.decorView as ViewGroup
                            val bitmap = createBitmap(view.width, view.height)
                            val canvas = Canvas(bitmap)
                            view.draw(canvas)
                            shadowView = ImageView(context).apply {
                                setImageBitmap(bitmap)
                                layoutParams = FrameLayout.LayoutParams(
                                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                                )
                            }
                            decorView.addView(shadowView)
                            containerWeight = decorView.width
                            containerHeight = decorView.height
                            startDrag = true
                        }
                        //1. 计算margin，view中心点和手指触摸位置对齐
                        //2. 边缘碰撞检测
                        shadowView?.apply {
                            layoutParams =
                                (layoutParams as FrameLayout.LayoutParams).apply {
                                    setMargins(
                                        ensureRange(
                                            event.rawX.toInt() - view.width / 2,
                                            0,
                                            containerWeight - view.width
                                        ),
                                        ensureRange(
                                            event.rawY.toInt() - view.height / 2,
                                            0,
                                            containerHeight - view.height
                                        ),
                                        0,
                                        0
                                    )
                                }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val decorView = requireActivity().window.decorView as ViewGroup
                        decorView.removeView(shadowView)
                        startDrag = false
                        shadowView = null
                    }
                }
                return true
            }
        })
    }

    /**
     * 确保值在最小和最大之间
     */
    private fun ensureRange(v: Int, min: Int, max: Int): Int {
        return if (v < min) {
            min
        } else if (v > max) {
            max
        } else {
            v
        }
    }

}