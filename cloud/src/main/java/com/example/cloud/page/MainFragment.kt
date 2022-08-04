package com.example.cloud.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.base.deeplink.WelcomeDeepLink
import com.example.cloud.R
import com.example.cloud.databinding.FragmentMainBinding
import com.example.cloud.page.components.FileListAdapter
import com.example.cloud.page.service.UploadService
import com.example.cloud.page.vm.CloudViewModel
import com.example.cloud.page.vm.UserViewModel
import com.example.repository.api.FileUploadListener
import com.example.repository.api.model.FileItem
import com.example.repository.api.model.User
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    val viewModel by viewModels<CloudViewModel>({ requireActivity() })
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
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.parentDirectory.fileItem = createDummyDirectory()
        binding.navigationView.setCheckedItem(R.id.main)

        fileListView.adapter = FileListAdapter(requireContext(), viewLifecycleOwner, viewModel)
        fileListView.layoutManager = LinearLayoutManager(requireContext())

        initDrawerMenu()

        initEvent()

        //监听用户切换
        userViewModel.currentUser.observe(viewLifecycleOwner) {
            viewModel.loadFiles()
        }

        selectFileLauncher = registerForActivityResult(OpenDocument(), this::onSelectFile)
        return binding.root
    }

    private fun initEvent() {
        initMenuClick()

        binding.uploadBtn.setOnClickListener {
            selectFileLauncher.launch(arrayOf("*/*"))
        }

        binding.appTopBar.setNavigationOnClickListener {
            binding.root.openDrawer(Gravity.START)
        }

        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)

        viewModel.currentDirectoryPath.observe(viewLifecycleOwner) {
            onBackPressedCallback.isEnabled = it != ""
        }

        viewModel.showParentDirectory.observe(viewLifecycleOwner) {
            if (it) {
                binding.parentDirectory.visibility = View.VISIBLE
            } else {
                binding.parentDirectory.visibility = View.GONE
            }
        }
    }

    private fun initDrawerMenu() {
        val headerView =
            LayoutInflater.from(context).inflate(R.layout.drawer_header_layout, null, false)

        binding.navigationView.addHeaderView(headerView)

        val userListView = headerView.findViewById<RecyclerView>(R.id.user_list)
        userListView.layoutManager = LinearLayoutManager(context)
        userListView.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            var users = listOf<User>()

            init {
                userViewModel.loadUsers().observeForever {
                    users = it
                    notifyDataSetChanged()
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                return object : ViewHolder(TextView(context)) {}
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val view = holder.itemView as TextView
                view.text = users[position].name
                view.setOnClickListener {
                    userViewModel.switchUser(users[position].id)
                }
            }

            override fun getItemCount(): Int {
                return users.size
            }

        }
    }

    private fun onSelectFile(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(context, "cancel", Toast.LENGTH_LONG).show()
            return
        }
        viewModel.uploadFile(uri, object : FileUploadListener {
            override fun onStart() {
                Log.d("######", "onProgress: start upload")

            }

            override fun onProgress(uploaded: Long, total: Long) {
                Log.d("######", "onProgress: $uploaded")
            }

            override fun onSuccess() {
                Log.d("######", "onProgress: 上传成功")
            }

            override fun onError(e: Exception) {
                Log.e("######", "onError: ", e)
            }

        })
        ContextCompat.startForegroundService(requireContext(),
            Intent(requireContext(), UploadService::class.java).apply {
                putExtra("uri", uri.toString())
            })
    }


    private fun createDummyDirectory(): FileItem {
        return FileItem("", "上一级", 0, 0, true, "DummyDirectory", 0)
    }

    private fun initMenuClick() {
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.drag_demo -> findNavController().navigate(R.id.action_mainFragment_to_dragDemoFragment)
                R.id.logout -> {
                    userViewModel.logout()
                    findNavController().navigate(WelcomeDeepLink)
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

}