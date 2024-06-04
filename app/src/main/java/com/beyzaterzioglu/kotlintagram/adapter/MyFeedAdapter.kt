package com.beyzaterzioglu.kotlintagram.adapter

import android.annotation.SuppressLint
import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.modal.Comments
import com.beyzaterzioglu.kotlintagram.modal.Feed
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import java.util.Date

interface OnCommentButtonClickListener {
    fun onCommentButtonClick(feed: Feed)
}
interface OnSubCommentButtonClickListener {
    fun onSubCommentButtonClick(feed: Feed)
}
class MyFeedAdapter : RecyclerView.Adapter<FeedHolder>(){



    var onCommentClickListener: ((String) -> Unit)? = null
    var onSubCommentClickListener:((String) -> Unit)? = null

    var feedList= listOf<Feed>()
    private var listener : onDoubleTappyClickListener?= null
    private var doubleTapListener: OnDoubleTapListener? = null
    private var commentButtonListener: OnCommentButtonClickListener? = null
    private var onSubCommentButtonClick: OnSubCommentButtonClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.feeditem,parent,false)
        return FeedHolder(view)
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: FeedHolder, position: Int) {
        //postun detayları
        val feed=feedList[position]
        var commentsAdapter = CommentsAdapter(emptyList())
        holder.commentsRecyclerView.adapter = commentsAdapter
        holder.commentsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        val boldText = "<b>${feed.username}</b>"
        val additionalText = ": ${feed.caption}"
        val combinedText = boldText + additionalText
        val formattedText: Spanned = Html.fromHtml(combinedText)
        //postun detayları
        holder.userNameCaption.text = formattedText
        val date = Date(feed.time!!.toLong() * 1000)
        //saat detayının uygun şekile getirilmesini sağlar.
        val instagramTimeFormat = DateUtils.getRelativeTimeSpanString(
            date.time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        )

        holder.time.setText(instagramTimeFormat)
        holder.userNamePoster.setText(feed.username)

        Glide.with(holder.itemView.context).load(feed.image).into(holder.feedImage) //asıl resim
        Glide.with(holder.itemView.context).load(feed.imageposter).into(holder.userPosterImage) //post atanın minik resmi

        holder.likecount.setText("${feed.likes} Likes")
        holder.onSubCommentClickListener = onSubCommentClickListener
        commentsAdapter.onSubCommentClickListener = onSubCommentClickListener


        holder.commentsRecyclerView.adapter = commentsAdapter
        holder.commentsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        loadComments(feed.postid!!, commentsAdapter)

        //iki kez dokununca olacaklar
        val doubleClickGestureDetector = GestureDetectorCompat(holder.itemView.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                listener?.onDoubleTap(feed)
                return true
            }
        })
     /*   holder.addSubCommentButton.setOnClickListener {
            onCommentClickListener?.invoke(feed.postid!!)
        } */
        holder.itemView.setOnTouchListener{_,event ->
            doubleClickGestureDetector.onTouchEvent(event)
            true
        }
        holder.addCommentButton.setOnClickListener {
            //commentButtonListener?.onCommentButtonClick(feed)
            onCommentClickListener?.invoke(feed.postid!!)
        }


    }



    fun updateFeedList(list: List<Feed>)
    {
        this.feedList=list
    }
    fun setListener(listener: onDoubleTappyClickListener){
        this.listener = listener
    }


    // Yorumları yüklemek için fonksiyon
    fun loadComments(postId: String, commentsAdapter: CommentsAdapter) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("LoadComments", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val commentsList = snapshot.toObjects(Comments::class.java)
                    commentsAdapter.updateComments(commentsList)
                    commentsAdapter.notifyDataSetChanged()
                }
            }
    }
  /*  fun loadSubComments(postId: String, commentsAdapter: CommentsAdapter) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Posts").document(postId).collection("comments")
            .document(commentId).collection("subcomments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("LoadComments", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val commentsList = snapshot.toObjects(Comments::class.java)
                    commentsAdapter.updateComments(commentsList)
                    commentsAdapter.notifyDataSetChanged()
                }
            }
    }

   */

}
class FeedHolder(itemView: View): ViewHolder(itemView)
{ //RecyclerView içindeki bir öğenin görünümünü temsil eder.

    var onSubCommentClickListener: ((String) -> Unit)? = null

    val userNamePoster : TextView = itemView.findViewById(R.id.feedtopusername)
    val userNameCaption : TextView = itemView.findViewById(R.id.feedusernamecaption)
    val commentsRecyclerView : RecyclerView = itemView.findViewById(R.id.commentsRecycler)

    val userPosterImage : CircleImageView = itemView.findViewById(R.id.userimage)
    val feedImage : ImageView = itemView.findViewById(R.id.feedImage)
    val time: TextView = itemView.findViewById(R.id.feedtime)
    val likecount: TextView = itemView.findViewById(R.id.likecount)
    val addCommentButton: Button = itemView.findViewById(R.id.commentbutton)
  //  val addSubCommentButton: Button = itemView.findViewById(R.id.answerCommentButton)
}

interface onDoubleTappyClickListener{
    //Çift dokunma etkinliğini dinlemek için bir arayüzü temsil eder.
    fun onDoubleTap(feed: Feed)
    // bu fonksiyon Feed türünden bir nesne alır,
    // böylece çift dokunmanın gerçekleştiği öğeye ilişkin verilere erişilebilir.
}