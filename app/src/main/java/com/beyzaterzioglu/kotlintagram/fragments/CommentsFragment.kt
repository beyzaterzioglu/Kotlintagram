package com.beyzaterzioglu.kotlintagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.adapter.CommentsAdapter
import com.beyzaterzioglu.kotlintagram.databinding.FragmentFullcommentsBinding
import com.beyzaterzioglu.kotlintagram.mvvm.ViewModel

class CommentsFragment : Fragment() {

    private lateinit var adapter: CommentsAdapter
    private lateinit var vm: ViewModel
    private lateinit var binding: FragmentFullcommentsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fullcomments, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(ViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        //adapter = CommentsAdapter()

        binding.commentsRecyclerView.adapter = adapter

        vm.getAllComments().observe(viewLifecycleOwner, Observer { comments ->
            adapter.updateComments(comments)
        })

        binding.backBtn.setOnClickListener {
            view.findNavController().navigateUp()
        }
    }
}
