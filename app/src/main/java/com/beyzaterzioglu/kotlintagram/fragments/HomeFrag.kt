package com.beyzaterzioglu.kotlintagram.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.Utils
import com.beyzaterzioglu.kotlintagram.activities.CommentsActivity
import com.beyzaterzioglu.kotlintagram.adapter.CommentsAdapter
import com.beyzaterzioglu.kotlintagram.adapter.MyFeedAdapter
import com.beyzaterzioglu.kotlintagram.adapter.onDoubleTappyClickListener
import com.beyzaterzioglu.kotlintagram.databinding.FragmentHomeBinding

import com.beyzaterzioglu.kotlintagram.modal.Comments
import com.beyzaterzioglu.kotlintagram.modal.Feed
import com.beyzaterzioglu.kotlintagram.mvvm.ViewModel
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFrag : Fragment(), onDoubleTappyClickListener {

    private lateinit var adapter: MyFeedAdapter
    private lateinit var vm: ViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val addCommentButton = view.findViewById<Button>(R.id.buttonAddComment)
        val commentEditText = view.findViewById<EditText>(R.id.editTextComment)

        adapter = MyFeedAdapter().apply {
            setListener(this@HomeFrag)
            //setCommentButtonClickListener(this@HomeFrag)
        }

        vm = ViewModelProvider(this).get(ViewModel::class.java)
        adapter = MyFeedAdapter()

        binding.lifecycleOwner = viewLifecycleOwner

        // RecyclerView ve Adapter ayarları
        binding.feedRecycler.adapter = adapter
        binding.feedRecycler.layoutManager = LinearLayoutManager(context)

        vm.loadMyFeed().observe(viewLifecycleOwner, Observer {
            adapter.updateFeedList(it)
            binding.feedRecycler.adapter = adapter
        })

        adapter.setListener(this)

        adapter.onCommentClickListener = {
            showAddCommentDialog(it)
        }

        binding.backButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFrag_to_profileFrag)
        }
        binding.imageViewBottom.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFrag_to_profileFrag)
        }


        vm.image.observe(viewLifecycleOwner, Observer {
            Glide.with(requireContext()).load(it).into(binding.imageViewBottom)
        })

        addCommentButton?.setOnClickListener {
            val commentText = commentEditText.text.toString()
            // Yorum ekleme fonksiyonunu çağırın
            addComment(vm.postId.value ?: "", Comments(vm.userId.value ?: "", "UserName", commentText))
        }

        // Yorumlar RecyclerView'u ve Adapter'i ayarlayın

        // Post ID'yi alın ve yorumları yükleyin (örnek bir post ID kullanıyorum)
        val postId = "examplePostId"
    }

    private fun showAddCommentDialog(postId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Yorum Ekle")

        val input = EditText(requireContext())
        builder.setView(input)

        builder.setPositiveButton("Gönder") { dialog, _ ->
            val commentText = input.text.toString()
            if (commentText.isNotEmpty()) {
                val comment = Comments(
                    userid = Utils.getUiLoggedIn(),
                    userName = "UserName", // Kullanıcı adını alın
                    commentText = commentText
                )
                addComment(postId, comment)
            }
        }
        builder.setNegativeButton("İptal") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    override fun onDoubleTap(feed: Feed) {
        val currentUserId = Utils.getUiLoggedIn()
        val postId = feed.postid
        val firestore = FirebaseFirestore.getInstance()
        val postRef = firestore.collection("Posts").document(postId!!)

        postRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val likes = document.getLong("likes")?.toInt() ?: 0
                    val likers = document.get("likers") as? List<String>

                    if (!likers.isNullOrEmpty() && likers.contains(currentUserId)) {
                        // Kullanıcı zaten beğenmiş
                        if(likes > 0){
                        postRef.update(
                            "likes", likes - 1,
                            "likers", FieldValue.arrayRemove(currentUserId)
                        ).addOnSuccessListener {
                            println("Post unliked!")
                        }.addOnFailureListener { exception ->
                            println("Failed to update like: $exception")
                        }}
                    } else {
                        // Beğeni sayısını artır ve beğenenler listesine ekle
                        postRef.update(
                            "likes", likes + 1,
                            "likers", FieldValue.arrayUnion(currentUserId)
                        ).addOnSuccessListener {
                            println("Post liked!")
                        }.addOnFailureListener { exception ->
                            println("Failed to update like: $exception")
                        }
                    }
                }
            }
    }


    // Yorum eklemek için fonksiyon
    fun addComment(postId: String, comment: Comments) {
        val firestore = FirebaseFirestore.getInstance()
        val commentRef = firestore.collection("Posts").document(postId).collection("comments").document()

        commentRef.set(comment)
            .addOnSuccessListener {
                Log.d("AddComment", "Comment added successfully")
            }
            .addOnFailureListener { e ->
                Log.w("AddComment", "Error adding comment", e)
            }
    }


    fun addSubComment(postId: String, commentId : String, comment: Comments) {
        val firestore = FirebaseFirestore.getInstance()
        val commentRef = firestore.collection("Posts").document(postId).collection("comments").document(commentId).collection("subcomments").document()

        commentRef.set(comment)
            .addOnSuccessListener {
                Log.d("AddComment", "Comment added successfully")
            }
            .addOnFailureListener { e ->
                Log.w("AddComment", "Error adding comment", e)
            }
    }
    fun onCommentButtonClick(feed: Feed) {
        val intent = Intent(activity, CommentsActivity::class.java).apply {
            putExtra("postId", feed.postid)
        }
        startActivity(intent)
    }

}
