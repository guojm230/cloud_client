package com.example.clouddemo.components

import android.content.Context
import android.graphics.Color
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.example.clouddemo.R
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.absoluteValue

private const val VERIFY_CODE_SIZE = 6

/**
 * 验证码选择框：
 * 1. 输入后自动跳转下一个框，并禁用前面的输入框
 * 2. 删除后可以自动跳转到前一个框
 * 3. 等间距排列
 * 4. 输入完毕的处理事件
 */
class VerifyCodeInput(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayoutCompat(context, attrs, defStyleAttr) {
    constructor(context: Context,attrs: AttributeSet?): this(context,attrs,0)

    private val children: List<FrameLayout>

    private var oldWidth: Int = 0
    private var oldHeight: Int = 0

    private var currentIndex = 0

    //listener
    var onCompleteListener: ((VerifyCodeInput,String)->Unit)? = null

    init {
        children = List(VERIFY_CODE_SIZE){ index->
            val view = TextInputEditText(context).apply {
                //设置minWidth，方便光标居中
                minWidth = 1
                setPadding(0,0,0,0)
                setBackgroundColor(Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT).apply {
                    gravity = Gravity.CENTER
                }
                //限制最大输入数量和输入类型
                inputType = InputType.TYPE_CLASS_NUMBER
                filters = arrayOf(LengthFilter(1))
            }
            //外层frame，方便添加边框和光标居中
            val frame = FrameLayout(context).apply {
                setPadding(5,5,5,5)
                background = context.getDrawable(R.drawable.login_input_border)
                addView(view)
            }

            initEvent(view, frame, index)
            addView(frame)

            if(index > 0){
                frame.isClickable = false
                view.isFocusable = false
            } else {
                //自动focus第一个输入框
                frame.post {
                    frame.callOnClick()
                }
            }
            return@List frame
        }
    }

    //重制状态
    fun reset(){
        children.forEachIndexed { index, frame ->
            val input = frame[0] as TextInputEditText
            input.setText("")
            if(index == 0){
                frame.isClickable = true
                input.isFocusable = true
                input.isFocusableInTouchMode = true
                frame.post {
                    frame.callOnClick()
                }
            } else {
                frame.isClickable = false
                input.isFocusable = false
            }
        }
    }

    private fun initEvent(view: TextInputEditText, frame: FrameLayout, index: Int){
        //对外层frame添加点击事件，避免input过小，点击不到
        frame.setOnClickListener {
            currentIndex = index
            frame[0].requestFocus()
            //invoke keyboard
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(frame[0],0)
        }
        //输入事件，输入后focus到下一个输入框
        view.doAfterTextChanged {
            val text = it?.toString() ?: ""
            if(text.isNotEmpty()){
                if(index == children.size - 1){
                    val value = children.joinToString { (it[0] as TextInputEditText).text?.toString() ?: "" }
                    //放入message队列，避免出现因为回调的执行导致最后一个input框数字显示不出来的情况
                    post {
                        onCompleteListener?.invoke(this,value)
                    }
                } else {
                    val nextFrame = children[index+1]
                    val nextInput = nextFrame[0]
                    //当前可不选中
                    frame.isClickable = false
                    frame[0].isFocusable = false

                    nextFrame.isClickable = true
                    //必须设置两个属性才能重新focus
                    nextInput.isFocusable = true
                    nextInput.isFocusableInTouchMode = true
                    //激活下一个
                    nextFrame.callOnClick()
                }
            }
        }

        //键盘删除事件，空白再次删除时focus到前一个input框
        view.setOnKeyListener { v, keyCode, event ->
            val text = (v as TextInputEditText).text?.toString() ?: ""
            if(keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN
                && text.isEmpty() && index > 0){
                val prevFrame = children[index-1]
                val prevInput = prevFrame[0] as TextInputEditText
                //删除上一个选择框的内容，重新可focus
                prevInput.setText("")
                prevFrame.isClickable = true
                prevInput.isFocusable = true
                prevInput.isFocusableInTouchMode = true
                prevFrame.callOnClick()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    /**
     * 在高度确定时，设置对应的margin和框高
     * 在onSizeChanged里也可以实现，但要在post里修改LayoutParams
     * 因为如果是layout过程触发onSizeChanged时，此时修改layoutParams是没用的，因为已经Measure过了
     * 要在本次layout完成后再触发一次才行。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        //等间距排列
        if(w != oldWidth || h != oldHeight){
            oldWidth = w
            oldHeight = h
            val margin = (w-(h*children.size))/(children.size - 1)
            children.forEachIndexed{ index,frame ->
                frame.layoutParams = (frame.layoutParams as LayoutParams).apply {
                    height = h
                    width = h
                    if (index > 0){
                        marginStart = margin
                    }
                }
                val view = frame[0] as TextInputEditText
                view.maxWidth = w
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


}