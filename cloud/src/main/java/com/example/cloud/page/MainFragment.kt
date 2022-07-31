package com.example.cloud.page

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cloud.databinding.FragmentMainBinding
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
        activity?.window?.setDecorFitsSystemWindows(true)
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.appTopBar.setNavigationOnClickListener {
            binding.root.openDrawer(Gravity.START)
        }

        viewModel.loadFiles(null).observe(viewLifecycleOwner) {
            fileListView.adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    return object : ViewHolder(TextView(requireContext())) {}
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val view = holder.itemView as TextView
                    view.text = it[position].name
                }

                override fun getItemCount(): Int {
                    return it.size
                }

            }
        }

        fileListView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }
}