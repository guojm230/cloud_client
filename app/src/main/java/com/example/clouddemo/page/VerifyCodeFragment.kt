package com.example.clouddemo.page

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.transition.TransitionInflater
import com.example.clouddemo.R
import com.example.clouddemo.databinding.FragmentVerifyCodeBinding


class VerifyCodeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var binding: FragmentVerifyCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterTransition = TransitionInflater.from(context!!).inflateTransition(R.transition.transition_bottom)
        val inputManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        binding =  FragmentVerifyCodeBinding.inflate(inflater,container,false)
        return binding.root
    }

}