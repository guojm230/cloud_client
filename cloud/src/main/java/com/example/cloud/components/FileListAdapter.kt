package com.example.cloud.components

import android.content.Context
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cloud.components.FileListAdapter.FileItemViewHolder
import com.example.cloud.vm.FileListViewModel
import com.example.repository.api.model.FileItem

/**
 * 负责展示文件列表和交互逻辑，以及触发的相应事件
 */
class FileListAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: FileListViewModel,
    private val nestedView: NestedScrollView
) : RecyclerView.Adapter<FileItemViewHolder>() {

    class FileItemViewHolder(fileItemView: View) : ViewHolder(fileItemView)

    var fileList: MutableList<FileItem> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var emptyView: View = TextView(context).apply {
        text = "empty data"
    }

    var minVisibleCount = 0
        private set

    var maxVisibleCount = 0
        private set

    private lateinit var recyclerView: RecyclerView

    init {
        viewModel.loadFiles().observe(lifecycleOwner) {
            fileList = it
            notifyDataSetChanged()
        }
        viewModel.isLoading.observe(lifecycleOwner) {
            emptyView.isVisible = !it
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        if (viewType == EMPTY_VIEW_TYPE) {
            return FileItemViewHolder(emptyView)
        }
        val itemView = FileItemView(context, null)
        itemView.layoutParams = FrameLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        return FileItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        if (holder.itemView !is FileItemView) {
            return
        }
        val itemView = holder.itemView as FileItemView
        itemView.fileItem = fileList[position]

        itemView.moveFileListener = { v, e ->
            if (e.action == FileItemView.ACTION_MOV_OUT) {
                if (e.to.isDirectory) {
                    viewModel.moveFile(e.from, e.to)
                }
            }
        }

        recyclerView.setOnDragListener { v, event ->
            Log.d(TAG, "onBindViewHolder: action:${event.action} 当前拖动的坐标(${event.x},${event.y})")
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    viewModel.showParentDirectory.value = viewModel.currentDirectoryPath.value != ""
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    viewModel.showParentDirectory.value = false
                }
            }
            return@setOnDragListener true
        }

        itemView.setOnClickListener {
            val item = fileList[position]
            if (!item.isDirectory) {
                viewModel.openFile(item)
            } else {
                viewModel.loadFiles(item.path)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (fileList.isEmpty()) {
            return EMPTY_VIEW_TYPE
        }
        return FILE_ITEM_VIEW_TYPE
    }

    override fun onViewAttachedToWindow(holder: FileItemViewHolder) {
        super.onViewAttachedToWindow(holder)
        maxVisibleCount = holder.adapterPosition
    }

    override fun onViewDetachedFromWindow(holder: FileItemViewHolder) {
        super.onViewDetachedFromWindow(holder)
        minVisibleCount = holder.adapterPosition
    }

    override fun getItemCount(): Int {
        if (fileList.isEmpty()) return 1
        return fileList.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    companion object {
        const val FILE_ITEM_VIEW_TYPE = 0
        const val EMPTY_VIEW_TYPE = 1

        val TAG = FileListAdapter::class.java.canonicalName
    }

}