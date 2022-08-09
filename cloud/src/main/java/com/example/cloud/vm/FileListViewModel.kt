package com.example.cloud.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.base.event.SingleEvent
import com.example.base.result.runPostError
import com.example.cloud.model.AlertDialogEvent
import com.example.repository.api.FileRepository
import com.example.repository.api.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileListViewModel @Inject constructor(
    val fileRepository: FileRepository
) : ViewModel() {

    private val _currentFileList = MutableLiveData<MutableList<FileItem>>()

    val currentFileList: LiveData<MutableList<FileItem>> = _currentFileList

    /**
     * 当前查询的路径，为空时表示已经到了根结点
     */
    private val _currentPath = MutableLiveData<String>()
    val currentDirectoryPath: LiveData<String> = _currentPath

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _showAlertDialog = MutableLiveData<SingleEvent<AlertDialogEvent>>()
    val showAlertDialog: LiveData<SingleEvent<AlertDialogEvent>> = _showAlertDialog

    val showParentDirectory = MutableLiveData(false)

    fun loadFiles(path: String = ""): LiveData<MutableList<FileItem>> {
        viewModelScope.launch {
            withLoading {
                val result = fileRepository.findFiles(path)
                result.runPostError {
                    _currentFileList.postValue(it.toMutableList())
                    _currentPath.postValue(path)
                }
            }
        }
        return currentFileList
    }

    fun openFile(fileItem: FileItem) {

    }

    fun moveFile(from: FileItem, to: FileItem,overwrite: Boolean = false) {
        viewModelScope.launch {
            var toFileItem = to
            if (to.type == "DummyDirectory") { //获取from的父级路径
                val directoryPath = from.path.substring(0, from.path.lastIndexOf("/"))
                toFileItem = to.copy(
                    path = directoryPath.substring(0, directoryPath.lastIndexOf("/")),
                    name = from.path.substring(from.path.lastIndexOf("/"))
                )
            }

            //检查文件是否已经存在，询问是否覆盖
            if (!overwrite){
                val targetPath = "${to.path}/${from.name}"
                fileRepository.findFileItem(targetPath).runPostError {
                    if (it != null){
                        _showAlertDialog.value = SingleEvent(AlertDialogEvent(
                            "文件已经存在",
                            "文件${targetPath}已经存在，是否覆盖？",
                            onConfirmCallback = {
                                moveFile(from, to, true)
                            }
                        ))
                        return@launch
                    }
                }
            }

            fileRepository.moveFile(from, toFileItem, overwrite).runPostError {
                loadFiles(_currentPath.value ?: "")
            }
        }
    }

    private inline fun withLoading(handler: ()->Unit){
        _isLoading.value = true
        handler()
        _isLoading.value = false
    }

}