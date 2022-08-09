package com.example.cloud.components

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.cloud.model.FileExistDialogModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileExistDialog(
    val context: Context,
    val model: FileExistDialogModel
) {
    private var dialog: AlertDialog? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun close(){
        coroutineScope.launch {
            if (dialog?.isShowing == true){
                dialog?.cancel()
            }
        }
    }

    fun show() {
        coroutineScope.launch(Dispatchers.Main) {
            dialog = MaterialAlertDialogBuilder(context).run {
                setTitle("文件已经存在")
                setMessage("文件${model.filePath}已经存在，是否覆盖？")
                setNeutralButton("取消"){ _, _ ->
                    model.onCancelCallback?.invoke()
                }
                setPositiveButton("确认"){ _, _ ->
                    model.onConfirmCallback?.invoke()
                }
                show()
            }
        }
    }

}