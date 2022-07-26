package com.example.clouddemo.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.example.clouddemo.R

/**
 * TODO: document your custom view class.
 */
class MyInput : View {

//    private var _exampleString: String? = null // TODO: use a default from R.string...
//    private var _exampleColor: Int = Color.RED // TODO: use a default from R.color...
//    private var _exampleDimension: Float = 0f // TODO: use a default from R.dimen...
//
//    private lateinit var textPaint: TextPaint
//    private var textWidth: Float = 0f
//    private var textHeight: Float = 0f
//
//    /**
//     * The text to draw
//     */
//    var exampleString: String?
//        get() = _exampleString
//        set(value) {
//            _exampleString = value
//            invalidateTextPaintAndMeasurements()
//        }
//
//    /**
//     * The font color
//     */
//    var exampleColor: Int
//        get() = _exampleColor
//        set(value) {
//            _exampleColor = value
//            invalidateTextPaintAndMeasurements()
//        }
//
//    /**
//     * In the example view, this dimension is the font size.
//     */
//    var exampleDimension: Float
//        get() = _exampleDimension
//        set(value) {
//            _exampleDimension = value
//            invalidateTextPaintAndMeasurements()
//        }
//
//    /**
//     * In the example view, this drawable is drawn above the text.
//     */
//    var exampleDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.MyInput, defStyle, 0
        )

//        _exampleString = a.getString(
//            R.styleable.MyInput_exampleString
//        )
//        _exampleColor = a.getColor(
//            R.styleable.MyInput_exampleColor,
//            exampleColor
//        )
//        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
//        // values that should fall on pixel boundaries.
//        _exampleDimension = a.getDimension(
//            R.styleable.MyInput_exampleDimension,
//            exampleDimension
//        )
//
//        if (a.hasValue(R.styleable.MyInput_exampleDrawable)) {
//            exampleDrawable = a.getDrawable(
//                R.styleable.MyInput_exampleDrawable
//            )
//            exampleDrawable?.callback = this
//        }

        a.recycle()

        // Set up a default TextPaint object
//        textPaint = TextPaint().apply {
//            flags = Paint.ANTI_ALIAS_FLAG
//            textAlign = Paint.Align.LEFT
//        }

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
//        textPaint.let {
//            it.textSize = exampleDimension
//            it.color = exampleColor
//            textWidth = it.measureText(exampleString)
//            textHeight = it.fontMetrics.bottom
//        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val p = Paint()
        p.color = Color.RED
        p.strokeWidth = 10F
        p.style = Paint.Style.FILL
//        canvas.drawLine(0F,0F, width.toFloat(),0F,p)
//        canvas.drawLine(width.toFloat(),0F,width.toFloat(),height.toFloat(),p)
//        canvas.drawLine(width.toFloat(),height.toFloat(),0F,height.toFloat(),p)
//        canvas.drawLine(0F,height.toFloat(),0F,0F,p)
        canvas.drawRect(0F,0F,width.toFloat(),height.toFloat(),p)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val i = 1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width: Int = widthMeasureSpec
        var height: Int = heightMeasureSpec
        if(MeasureSpec.getSize(widthMeasureSpec) > 100){
            width = MeasureSpec.makeMeasureSpec(100,MeasureSpec.EXACTLY)
        }

        if(MeasureSpec.getSize(heightMeasureSpec) > 100){
            height = MeasureSpec.makeMeasureSpec(100,MeasureSpec.EXACTLY)
        }
        setMeasuredDimension(width, height)
    }
}