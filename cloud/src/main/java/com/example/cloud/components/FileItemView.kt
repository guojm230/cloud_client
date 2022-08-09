package com.example.cloud.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.cloud.R
import com.example.cloud.databinding.FileItemLayoutBinding
import com.example.repository.api.model.FileItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 文件展示组件，负责:
 * 1. 监听各种事件，如：拖拽事件、点击事件
 * 2. 根据不同的文件类型显示不同的图标
 * 3. 根据不同的事件做出不同的UI展示
 */
class FileItemView(
    context: Context, fileItem: FileItem?, attrs: AttributeSet?
) : FrameLayout(context, attrs) {

    constructor(context: Context, attrs: AttributeSet) : this(context, null, attrs)

    constructor(context: Context, fileItem: FileItem?) : this(context, fileItem, null)

    private val binding: FileItemLayoutBinding

    private var _fileItem: FileItem? = fileItem
    var fileItem: FileItem?
        get() = _fileItem
        set(value) {
            _fileItem = value
            fileItem?.also {
                updateView(it)
            }
        }

    var moveFileListener: ((v: FileItemView, event: MoveFileEvent) -> Unit)? = null

    var startDragListener: (() -> Unit)? = null

    init {
        binding = FileItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        initDragEvent()
        fileItem?.also {
            updateView(it)
        }
    }

    private fun initDragEvent() { //只有文件夹接收该事件
        /**
         * 对于拖拽事件，有一方触发MoveFileEvent即可，
         * 这里选择让接收方触发MoveFileEvent
         */
        setOnDragListener { v, event ->
            if (fileItem?.isDirectory != true) {
                return@setOnDragListener false
            }
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    //通过回调通知发起者，由发起者调用listener
                    (event.localState as ((FileItem) -> Unit))(fileItem!!)
                }
                DragEvent.ACTION_DRAG_ENTERED -> { //TODO 添加hover UI

                }
                DragEvent.ACTION_DRAG_EXITED, DragEvent.ACTION_DRAG_ENDED -> { //TODO 移除hoverUI

                }
            }
            return@setOnDragListener true
        }

        setOnLongClickListener {
            val shadowBuilder = DragShadowBuilder(this)
            //Recycler View会不断的bind，可能会导致回调时对象里的item已经被修改为其他值，所以提前用局部变量捕获一下
            //实际上，回调里传入的this对象也可能和发起移动文件的FileItem对不上了，不过项目中没用到，就先不处理了
            val fileItem = fileItem
            startDragAndDrop(null, shadowBuilder, { to: FileItem ->
                if (to != fileItem) {
                    moveFileListener?.invoke(
                        this, MoveFileEvent(
                            ACTION_MOV_OUT, fileItem!!, to
                        )
                    )
                }
            }, 0)
            startDragListener?.invoke()
            return@setOnLongClickListener true
        }
    }

    private fun updateView(fileItem: FileItem) {
        with(binding) {
            fileIcon.setImageDrawable(findIcon(fileItem))
            fileNameTextView.text = fileItem.name
            if (isDummyDirectory(fileItem)) { //拖动时临时存在的上一级文件夹
                binding.fileInfoLayout.visibility = View.GONE
                binding.dirArrow.visibility = View.INVISIBLE
                fileNameTextView.layoutParams = fileNameTextView.layoutParams.apply {
                    height = LayoutParams.MATCH_PARENT
                }
                return
            }

            binding.fileInfoLayout.visibility = View.VISIBLE
            binding.dirArrow.visibility = View.VISIBLE

            fileNameTextView.layoutParams = fileNameTextView.layoutParams.apply {
                height = LayoutParams.WRAP_CONTENT
            }

            childrenSizeTextView.text = if (fileItem.isDirectory) {
                "${fileItem.childrenSize}项"
            } else {
                convertFileLength(fileItem.length)
            }
            modifyDateTextView.text = dateTimeFormatter.format(
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fileItem.lastModified), ZoneId.systemDefault()
                )
            )
            binding.dirArrow.isVisible = fileItem.isDirectory
        }
    }

    private fun isDummyDirectory(fileItem: FileItem): Boolean {
        return fileItem.type.split("/")[0] == "DummyDirectory"
    }

    private fun findIcon(fileItem: FileItem): Drawable {
        if (fileItem.isDirectory) {
            return context.getDrawable(R.drawable.icon_folder)!!
        }
        val mainType = fileItem.type.split("/")[0]
        val drawableId = when (mainType) {
            "text" -> R.drawable.icon_text
            else -> R.drawable.icon_file_unknown
        }
        return context.getDrawable(drawableId)!!
    }

    private fun convertFileLength(length: Long): String {
        return when (length) {
            in 0 until KB -> "${length}B"
            in KB until MB -> "${length / KB}KB"
            in MB until GB -> "${length / MB}MB"
            else -> "${length / GB}GB"
        }
    }

    /**
     * 文件被移动时触发
     */
    class MoveFileEvent( //mov in or mov out
        val action: Int, val from: FileItem, val to: FileItem
    )

    companion object {

        const val ACTION_MOV_IN = 1
        const val ACTION_MOV_OUT = 2

        const val KB = 1024
        const val MB = 1024 * 1024
        const val GB = 1024 * 1024 * 1024

        @JvmStatic
        val dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY/M/d HH:mm:ss")
    }

}