package com.beyzaterzioglu.kotlintagram.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.Utils
import com.beyzaterzioglu.kotlintagram.adapter.CommentsAdapter
import com.beyzaterzioglu.kotlintagram.modal.Comments
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers

class CommentsActivity : AppCompatActivity() {

    private lateinit var commentsAdapter: CommentsAdapter
    private val commentsList = mutableListOf<Comments>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val postId = intent.getStringExtra("postId") ?: return
        val commentId=intent.getStringExtra("commentId")?: return

        commentsAdapter = CommentsAdapter(commentsList)
        findViewById<RecyclerView>(R.id.commentsRecycler).apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
            adapter = commentsAdapter
        }

        loadComments(postId, commentsAdapter)


    }

    private fun showAddCommentDialog(postId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Yorum Ekle")

        val input = EditText(this)
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

    private fun addComment(postId: String, comment: Comments) {
        val firestore = FirebaseFirestore.getInstance()
        val commentRef = firestore.collection("posts").document(postId).collection("comments").document()

        commentRef.set(comment)
            .addOnSuccessListener {
                Log.d("AddComment", "Comment added successfully")
            }
            .addOnFailureListener { e ->
                Log.w("AddComment", "Error adding comment", e)
            }
    }

    private fun loadComments(postId: String, commentsAdapter: CommentsAdapter) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("LoadComments", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val commentsList = snapshot.toObjects(Comments::class.java)
                    commentsAdapter.updateComments(commentsList)
                }
            }
    }

}
