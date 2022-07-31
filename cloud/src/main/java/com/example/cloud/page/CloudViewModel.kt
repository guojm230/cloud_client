package com.example.cloud.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.api.FileRepository
import com.example.repository.api.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudViewModel @Inject constructor(
    val fileRepository: FileRepository
) : ViewModel() {

    private val _currentFileList = MutableLiveData<List<FileItem>>()

    val currentFileList: LiveData<List<FileItem>> = _currentFileList

    fun loadFiles(path: String?): LiveData<List<FileItem>> {
        viewModelScope.launch {
            val result = fileRepository.findFiles(path ?: "")
            if (result.isSuccess) {
                _currentFileList.postValue(result.data)
            } else {
                println("加载失败")
            }
        }
        return currentFileList
    }

}