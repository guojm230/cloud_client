package com.example.cloud.page.components

import android.content.ClipData
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
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

    /**
     * 文件移入监听器
     */
    var eventListener: ((FileEvent) -> Unit)? = null

    init {
        binding = FileItemLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        initDragEvent()
        fileItem?.also {
            updateView(it)
        }
    }

    private fun initDragEvent() { //只有文件夹接收该事件
        setOnDragListener { v, event ->
            if (fileItem?.isDirectory != true) {
                return@setOnDragListener false
            }

            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    eventListener?.invoke(MoveInEvent(fileItem!!, event.localState as FileItem))
                    eventListener?.invoke(MoveOutEvent(event.localState as FileItem, fileItem!!))
                }
                DragEvent.ACTION_DRAG_ENTERED -> { //TODO 添加hover UI
                }
                DragEvent.ACTION_DRAG_EXITED, DragEvent.ACTION_DRAG_ENDED -> { //TODO 移除hoverUI
                }
            }
            return@setOnDragListener true
        }

        setOnClickListener {
            if (fileItem != null) {
                eventListener?.invoke(ClickEvent(fileItem!!))
            }
        }

        setOnLongClickListener {
            val shadowBuilder = DragShadowBuilder(this)
            startDragAndDrop(ClipData.newPlainText("", ""), shadowBuilder, fileItem, 0)
            return@setOnLongClickListener true
        }
    }

    private fun updateView(fileItem: FileItem) {
        with(binding) {
            fileIcon.setImageDrawable(findIcon(fileItem))
            fileNameTextView.text = fileItem.name
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

    private fun findIcon(fileItem: FileItem): Drawable {
        if (fileItem.isDirectory) {
            return context.getDrawable(R.drawable.ic_baseline_folder_24)!!
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
     * 代表View可以探测到的和文件操纵有关的事件，如：
     * 移入、点击等
     * 用sealed class代替Action标志
     */
    sealed interface FileEvent {
        val target: FileItem //代表触发事件的自身item
    }

    /**
     * 有文件移入时触发，只对文件夹有用
     */
    class MoveInEvent(
        override val target: FileItem, val moveOut: FileItem
    ) : FileEvent

    /**
     * 自身文件被移动到其他地方时触发
     */
    class MoveOutEvent(
        override val target: FileItem, val moveIn: FileItem
    ) : FileEvent

    class ClickEvent(
        override val target: FileItem
    ) : FileEvent


    companion object {

        const val KB = 1024
        const val MB = 1024 * 1024
        const val GB = 1024 * 1024 * 1024

        @JvmStatic
        val dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY/M/d HH:mm:ss")
    }

}