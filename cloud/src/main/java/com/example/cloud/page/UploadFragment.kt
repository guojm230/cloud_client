package com.example.cloud.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cloud.R
import com.example.cloud.vm.UploadViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 上传文件列表展示页面
 */
@AndroidEntryPoint
class UploadFragment : Fragment() {

    private lateinit var viewModel: UploadViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload, container, false)
    }




}