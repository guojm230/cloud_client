package com.example.clouddemo.components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import com.example.clouddemo.R
import com.google.android.material.textfield.TextInputEditText

class VerifyCodeInput(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayoutCompat(context, attrs, defStyleAttr) {
    constructor(context: Context,attrs: AttributeSet?): this(context,attrs,0)

    val children: List<TextInputEditText>

    init {
        children = List(6){
            val view = TextInputEditText(context).apply {
                setPadding(0,0,0,0)
                background = context.getDrawable(R.drawable.login_input_border)
            }
            addView(view)
            return@List view
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val margin = (w-(h*children.size))/(children.size - 1)
        children.forEachIndexed{ index,view ->
            view.layoutParams = (view.layoutParams as LayoutParams).apply {
                height = h
                width = h
                if (index > 0){
                    marginStart = margin
                }
            }
        }
    }

}