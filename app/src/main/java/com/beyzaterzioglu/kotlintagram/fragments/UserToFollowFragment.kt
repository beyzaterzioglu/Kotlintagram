package com.beyzaterzioglu.kotlintagram.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.adapter.OnFriendClicked
import com.beyzaterzioglu.kotlintagram.mvvm.ViewModel
import com.beyzaterzioglu.kotlintagram.adapter.UsersAdapter
import com.beyzaterzioglu.kotlintagram.databinding.FragmentUserToFollowBinding
import com.beyzaterzioglu.kotlintagram.modal.Users
import com.google.firebase.firestore.auth.User


class UserToFollowFragment : Fragment(),OnFriendClicked {

    private lateinit var adapter : UsersAdapter
    private lateinit var vm : ViewModel
    private lateinit var binding: FragmentUserToFollowBinding
    var clickedOn: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_user_to_follow, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //vm = ViewModelProvider(this).get(ViewModel::class.java)
        vm = ViewModelProvider(this).get(ViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter= UsersAdapter()

        binding.backBtn.setOnClickListener {

            view.findNavController().navigate(R.id.action_userToFollowFragment_to_profileFrag)

        }

        vm.getAllUsers().observe(viewLifecycleOwner,Observer{
            //takip edilen kişileri listeler.
            adapter.setUserList(it)
            binding.rvFollow.adapter=adapter
        })
        adapter.setListener(this)


    }


    override fun onfriendListener(position: Int, user: Users) {
        //birisi takip edildiğinde followUser fonksiyonu çağırılır.
        adapter.followUser(user)
    }

}

