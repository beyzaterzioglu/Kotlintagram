package com.beyzaterzioglu.kotlintagram.adapter

import android.annotation.SuppressLint
import android.text.Html
import android.text.Spanned
import android.text.format.DateUtils
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.modal.Feed
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Date


class MyFeedAdapter : RecyclerView.Adapter<FeedHolder>(){

    var feedList= listOf<Feed>()
    private var listener : onDoubleTappyClickListener?= null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.feeditem,parent,false)
        return FeedHolder(view)
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FeedHolder, position: Int) {

        val feed=feedList[position]

        val boldText = "<b>${feed.username}</b>"
        val additionalText = ": ${feed.caption}"
        val combinedText = boldText + additionalText
        val formattedText: Spanned = Html.fromHtml(combinedText)


        holder.userNameCaption.text = formattedText



        val date = Date(feed.time!!.toLong() * 1000)

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


        //bak
        val doubleClickGestureDetector = GestureDetectorCompat(holder.itemView.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                listener?.onDoubleTap(feed)
                return true
            }
        })

        holder.itemView.setOnTouchListener{_,event ->
            doubleClickGestureDetector.onTouchEvent(event)
            true
        }
    }

    fun updateFeedList(list: List<Feed>)
    {
        this.feedList=list
    }
    fun setListener(listener: onDoubleTappyClickListener){
        this.listener = listener
    }

}
class FeedHolder(itemView: View): ViewHolder(itemView)
{
    val userNamePoster : TextView = itemView.findViewById(R.id.feedtopusername)
    val userNameCaption : TextView = itemView.findViewById(R.id.feedusernamecaption)

    val userPosterImage : CircleImageView = itemView.findViewById(R.id.userimage)
    val feedImage : ImageView = itemView.findViewById(R.id.feedImage)
    val time: TextView = itemView.findViewById(R.id.feedtime)
    val likecount: TextView = itemView.findViewById(R.id.likecount)
}

interface onDoubleTappyClickListener{
    fun onDoubleTap(feed: Feed)
}