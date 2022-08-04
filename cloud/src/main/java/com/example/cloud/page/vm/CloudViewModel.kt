package com.example.cloud.page.vm

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.api.FileRepository
import com.example.repository.api.FileUploadListener
import com.example.repository.api.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudViewModel @Inject constructor(
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

    val showParentDirectory = MutableLiveData(false)

    fun loadFiles(path: String = ""): LiveData<MutableList<FileItem>> {
        _isLoading.value = true
        viewModelScope.launch {
            val result = fileRepository.findFiles(path)
            if (result.isSuccess) {
                _currentFileList.postValue(result.data!!.toMutableList())
                _currentPath.postValue(path)
            } else {
                println("加载失败")
            }
            _isLoading.postValue(false)
        }
        return currentFileList
    }

    fun openFile(fileItem: FileItem) {

    }

    fun moveFile(from: FileItem, to: FileItem) {
        viewModelScope.launch {
            var toFile = to
            if (to.type == "DummyDirectory") { //获取from的父级路径
                val directoryPath = from.path.substring(0, from.path.lastIndexOf("/"))
                toFile = to.copy(
                    path = directoryPath.substring(0, directoryPath.lastIndexOf("/")),
                    name = from.path.substring(from.path.lastIndexOf("/"))
                )
            }
            val result = fileRepository.moveFile(from, toFile, false)
            if (result.isSuccess) {
                loadFiles(_currentPath.value ?: "")
            } else {
                println("error")
            }
        }
    }

    fun uploadFile(uri: Uri, listener: FileUploadListener) {
        fileRepository.uploadFile(uri, currentDirectoryPath.value ?: "", false, listener)
    }

}