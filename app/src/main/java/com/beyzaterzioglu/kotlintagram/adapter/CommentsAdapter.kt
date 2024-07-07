package com.beyzaterzioglu.kotlintagram.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.modal.Comments
import org.w3c.dom.Comment


import java.text.DateFormat

import java.text.SimpleDateFormat
import java.util.Date

class CommentsAdapter(
    private var commentsList: List<Comments>,

    ) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {
    var onSubCommentClickListener: ((String) -> Unit)? = null

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.textViewUsername)
        val commentText: TextView = itemView.findViewById(R.id.textViewComment)
        val timestamp: TextView = itemView.findViewById(R.id.textViewtime)
        val addSubCommentButton: TextView = itemView.findViewById(R.id.answerCommentButton)

        init {
            // Alt yorum ekleme düğmesine tıklama olayını burada tanımlayın
            addSubCommentButton.setOnClickListener {
                val position = adapterPosition // Tıklanan öğenin pozisyonunu alın
                onSubCommentButtonClick(position) // Tanımlanan onClickListener'ı çağırın
            }
        }
        fun onSubCommentButtonClick(position: Int) {

        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentsList[position]
        holder.userName.text = comment.userName
        holder.commentText.text = comment.commentText

        // Timestamp'i Date'e çevir ve formatla
        val date = comment.timestamp.toDate()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy (HH:mm:ss)", java.util.Locale.getDefault())
        holder.timestamp.text = dateFormat.format(date)

    }

    override fun getItemCount() = commentsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateComments(newCommentsList: List<Comments>) {
        commentsList = newCommentsList.take(10) // Yalnızca ilk 10 yorumu al
        notifyDataSetChanged()
    }
}
