package com.beyzaterzioglu.kotlintagram.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beyzaterzioglu.kotlintagram.R
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.beyzaterzioglu.kotlintagram.modal.Posts
import com.bumptech.glide.Glide

class MyPostAdapter: RecyclerView.Adapter<PostHolder>() {
    var mypostlist=listOf<Posts>()  // RecyclerView için kullanılacak olan post listesi

    //yenbi bir view oluştuğunda çağırılır
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.postitems,parent,false)
        return PostHolder(view)
    }

    override fun getItemCount(): Int {
       return mypostlist.size //toplam öge sayısı
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post=mypostlist[position] //veri bağlama
        Glide.with(holder.itemView.context).load(post.image).into(holder.image)
        //image henüz yüklenmediği için kırmızı
    }
    fun setPostList(list: List<Posts>){
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(mypostlist, list))
        mypostlist = list
        diffResult.dispatchUpdatesTo(this)
    }



}
class PostHolder(itemView: View) : ViewHolder(itemView)
{
 val image : ImageView =itemView.findViewById(R.id.postImage)
}
class MyDiffCallback( // Yeni ve eski veri listeleri arasındaki farkları belirlemek için kullanılır
    // amaç veri değişikliklerini algılayıp ona göre uygulamayı düzenlemektir.
    private val oldList: List<Posts>,
    private val newList: List<Posts>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Öğelerin benzersizliğini kontrol eder
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Öğelerin benzersizliğini kontrol eder
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}