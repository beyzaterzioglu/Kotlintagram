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
import com.beyzaterzioglu.kotlintagram.Utils
import com.beyzaterzioglu.kotlintagram.adapter.MyFeedAdapter
import com.beyzaterzioglu.kotlintagram.adapter.UsersAdapter

import com.beyzaterzioglu.kotlintagram.adapter.onDoubleTappyClickListener
import com.beyzaterzioglu.kotlintagram.databinding.FragmentHomeBinding
import com.beyzaterzioglu.kotlintagram.databinding.FragmentUserToFollowBinding
import com.beyzaterzioglu.kotlintagram.modal.Feed
import com.beyzaterzioglu.kotlintagram.mvvm.ViewModel
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class HomeFrag : Fragment(), onDoubleTappyClickListener {
    private lateinit var adapter : MyFeedAdapter
    private lateinit var vm : ViewModel
    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(ViewModel::class.java)
        adapter = MyFeedAdapter()

        binding.lifecycleOwner = viewLifecycleOwner


        vm.loadMyFeed().observe(viewLifecycleOwner, Observer {


            adapter.updateFeedList(it)

            binding.feedRecycler.adapter = adapter



        } )
        adapter.setListener(this)
        binding.backButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFrag_to_profileFrag)
        }
        binding.imageViewBottom.setOnClickListener {

            view.findNavController().navigate(R.id.action_homeFrag_to_profileFrag)

        }

        vm.image.observe(viewLifecycleOwner, Observer {


            Glide.with(requireContext()).load(it).into(binding.imageViewBottom)


        })



    }

    override fun onDoubleTap(feed: Feed) {
      val currentUserId=Utils.getUiLoggedIn()
        val postId=feed.postid
        val firestore=FirebaseFirestore.getInstance()
        val postRef = firestore.collection("Posts").document(postId!!)

        postRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val likes = document.getLong("likes")?.toInt() ?: 0
                    val likers = document.get("likers") as? List<String>

                    if (!likers.isNullOrEmpty() && likers.contains(currentUserId)) {
                        // User has already liked the post
                        println("You have already liked this post!")
                    } else {
                        // Increment like count and update likers
                        postRef.update(
                            "likes", likes + 1,
                            "likers", FieldValue.arrayUnion(currentUserId)
                        )
                            .addOnSuccessListener {
                                println("Post liked!")
                            }
                            .addOnFailureListener { exception ->
                                println("Failed to update like: $exception")
                            }
                    }
                }
            }

    }


}