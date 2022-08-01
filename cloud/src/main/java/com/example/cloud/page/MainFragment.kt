package com.example.cloud.page

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cloud.R
import com.example.cloud.databinding.FragmentMainBinding
import com.example.cloud.page.components.FileItemView
import com.example.cloud.page.components.FileItemView.ClickEvent
import com.example.cloud.page.components.FileItemView.MoveInEvent
import com.example.cloud.page.components.FileItemView.MoveOutEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    val viewModel by viewModels<CloudViewModel>({ requireActivity() })

    val fileListView get() = binding.fileListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.appTopBar.setNavigationOnClickListener {
            binding.root.openDrawer(Gravity.START)
        }

        viewModel.loadFiles(null).observe(viewLifecycleOwner) { it ->
            fileListView.adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view = FileItemView(requireContext(), null)
                    view.layoutParams = FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
                    )
                    return object : ViewHolder(view) {}
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val view = holder.itemView as FileItemView
                    view.fileItem = it[position]
                    view.eventListener = { e ->
                        when (e) {
                            is ClickEvent -> {
                                if (e.target.isDirectory) {
                                    viewModel.loadFiles(e.target.path)
                                }
                            }
                            is MoveOutEvent -> {}
                            is MoveInEvent -> {}
                        }
                    }
                }

                override fun getItemCount(): Int {
                    return it.size
                }

            }

            binding.navigationView.setCheckedItem(R.id.main)
        }

        fileListView.layoutManager = LinearLayoutManager(requireContext())

        initMenuClick()

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.currentDirectoryPath != "") {
                    viewModel.loadFiles(
                        viewModel.currentDirectoryPath.substring(
                            0, viewModel.currentDirectoryPath.lastIndexOf("/")
                        )
                    )
                }
            }
        })

        return binding.root
    }

    private fun initMenuClick() {
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.drag_demo -> findNavController().navigate(R.id.action_mainFragment_to_dragDemoFragment)
            }
            return@setNavigationItemSelectedListener true
        }
    }

}