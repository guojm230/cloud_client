package com.example.cloud.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.base.event.GlobalEvents
import com.example.base.event.UploadSuccessEvent
import com.example.base.event.consume
import com.example.base.nav.clearAndNavigate
import com.example.base.nav.deeplink.ACTION_BACK
import com.example.base.nav.deeplink.WelcomeDeepLink
import com.example.base.nav.deeplink.createSelectUserDeepLink
import com.example.cloud.R
import com.example.cloud.components.FileListAdapter
import com.example.cloud.databinding.FragmentMainBinding
import com.example.cloud.model.AlertDialogEvent
import com.example.cloud.model.StartUploadServiceEvent
import com.example.cloud.service.UploadService
import com.example.cloud.vm.FileListViewModel
import com.example.cloud.vm.UserViewModel
import com.example.repository.api.model.FileItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    val viewModel by viewModels<FileListViewModel>({ requireActivity() })
    val userViewModel by viewModels<UserViewModel>({ requireActivity() })

    private val fileListView get() = binding.fileListView

    //监听返回事件
    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (viewModel.currentDirectoryPath.value != "") {
                viewModel.loadFiles(
                    viewModel.currentDirectoryPath.value!!.substring(
                        0, viewModel.currentDirectoryPath.value!!.lastIndexOf("/")
                    )
                )
            }
        }
    }

    private lateinit var selectFileLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.parentDirectory.fileItem = createDummyDirectory()
        binding.navigationView.setCheckedItem(R.id.main)

        fileListView.adapter = FileListAdapter(requireContext(), viewLifecycleOwner, viewModel)
        fileListView.layoutManager = LinearLayoutManager(requireContext())

        selectFileLauncher = registerForActivityResult(OpenDocument(), this::onSelectFile)

        initDrawerMenu()

        initEvent()

        return binding.root
    }

    private fun initEvent() {
        initMenuNavigation()
        //选择按钮
        binding.uploadBtn.setOnClickListener {
            selectFileLauncher.launch(arrayOf("*/*"))
        }
        //菜单按钮
        binding.appTopBar.setNavigationOnClickListener {
            binding.root.openDrawer(Gravity.START)
        }
        //监听用户切换
        userViewModel.currentUser.observe(viewLifecycleOwner) {
            viewModel.loadFiles()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }

        viewModel.currentDirectoryPath.observe(viewLifecycleOwner) {
            binding.appTopBar.title = it.substring(it.lastIndexOf("/") + 1)
            //最外层文件夹时禁用Fragment返回键,调用系统的返回键
            onBackPressedCallback.isEnabled = it != ""
        }

        //拖动时显示上一级文件夹
        viewModel.showParentDirectory.observe(viewLifecycleOwner) {
            binding.parentDirectory.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        //显示提示框
        viewModel.alertDialogEvent.consume(viewLifecycleOwner, this::showFileExistDialog)
        //启动service
        viewModel.startUploadEvent.consume(viewLifecycleOwner, this::startUploadService)

        //上传完成时刷新当前文件夹
        GlobalEvents.uploadEvent.consume(viewLifecycleOwner) {
            val currentPath = viewModel.currentDirectoryPath.value
            if (it is UploadSuccessEvent && it.uploadPath == currentPath) {
                viewModel.loadFiles(currentPath)
            }
        }
        //添加返回键拦截
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun initDrawerMenu() {
        val headerView =
            LayoutInflater.from(context).inflate(R.layout.drawer_header_layout, null, false)

        binding.navigationView.addHeaderView(headerView)

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            headerView.findViewById<MaterialTextView>(R.id.name_text_view).text = it.name
        }

        userViewModel.currentAccount.observe(viewLifecycleOwner) {
            headerView.findViewById<MaterialTextView>(R.id.email_text_view).text = it.email
        }

    }

    private fun onSelectFile(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(context, "cancel", Toast.LENGTH_LONG).show()
            return
        }
        viewModel.uploadFile(uri, false)
    }

    private fun showFileExistDialog(event: AlertDialogEvent) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(event.title)
            setMessage(event.message)
            setNeutralButton("取消") { dialog, which ->
                event.onCancelCallback?.invoke()
            }
            setPositiveButton("确认") { dialog, which ->
                event.onConfirmCallback?.invoke()
            }
            show()
        }
    }

    private fun startUploadService(event: StartUploadServiceEvent) {
        ContextCompat.startForegroundService(requireContext(),
            Intent(requireContext(), UploadService::class.java).apply {
                putExtra("uri", event.uri.toString())
                putExtra("path", event.path)
                putExtra("overwrite", event.overwrite)
            })
    }


    private fun createDummyDirectory(): FileItem {
        return FileItem("", "上一级", 0, 0, true, "DummyDirectory", 0)
    }

    private fun initMenuNavigation() {
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.drag_demo -> findNavController().navigate(R.id.action_mainFragment_to_dragDemoFragment)
                R.id.download -> findNavController().navigate(R.id.action_mainFragment_to_downloadFragment)
                R.id.upload -> findNavController().navigate(R.id.action_mainFragment_to_uploadFragment)
                R.id.logout -> {
                    userViewModel.logout()
                    findNavController().clearAndNavigate(WelcomeDeepLink)
                }
                R.id.switch_user -> {
                    findNavController().navigate(
                        createSelectUserDeepLink(
                            ACTION_BACK
                        )
                    )
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

}