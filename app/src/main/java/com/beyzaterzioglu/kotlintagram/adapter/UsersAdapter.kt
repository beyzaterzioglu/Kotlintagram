package com.beyzaterzioglu.kotlintagram.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.Utils
import com.beyzaterzioglu.kotlintagram.modal.Users
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter : RecyclerView.Adapter<UserHolder>() {

    var listofusers= listOf<Users>()
    private var listener : OnFriendClicked?=null
    var clickedOn: Boolean=false
    var follower_id=""
    var following_id=""
    var followingcount : Int=0



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.userlist,parent,false)
        return UserHolder(view)
    }

    override fun getItemCount(): Int {
        return listofusers.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {

        val user=listofusers[position]
        holder.userName.text=user.username
        Glide.with(holder.itemView.context).load(user.image).into(holder.userimage)

        val firestore=FirebaseFirestore.getInstance()
        firestore.collection("Follow").document(Utils.getUiLoggedIn()).addSnapshotListener{value,error ->
            if(error!=null)
            {
                return@addSnapshotListener
            }
            if(value!!.exists() && value!=null)
            {
                val followingids=value.get("following_id") as? List<String>
                if(followingids!=null){
                    if(followingids.contains(user.userid))
                        holder.toggle.isChecked=true
                }
                else
                {
                    holder.toggle.isChecked=false
                }
            }
        }
        holder.toggle.setOnClickListener{
            val isChecked=holder.toggle.isChecked
            if(isChecked)
            {

                followUser(user)
            }
            else
            {
                unfollowUser(user)
            }
        }



    }

    //follow
    fun followUser(user:Users){
        val firestore=FirebaseFirestore.getInstance()
        val userdocref=firestore.collection("Users").document(Utils.getUiLoggedIn())
        //takip edilenleri arttırır.
        userdocref.update("following",FieldValue.increment(1))

        //takipçi sayısını arttırır takip eidlen kişinin
        val usertofollow=firestore.collection("Users").document(user.userid!!)
        usertofollow.update("followers",FieldValue.increment(1))

        val followDocRef=firestore.collection("Follow").document(Utils.getUiLoggedIn())
        followDocRef.get().addOnSuccessListener { documentsnapshot->
            if(documentsnapshot.exists()){
                val existingIds=documentsnapshot.get("following_id") as? List<String>
                val newIds=existingIds?.toMutableList()?: mutableListOf()
                newIds.add(user.userid!!)

                followDocRef.update(hashMapOf("following_id" to newIds )as Map<String,Any>)
            }
            else
            {
                //koleksiyon yoksa kimseyi takip etmiyoruzdur.

                val followData= hashMapOf("following_id" to listOf(user.userid!!)) //yeni takip edilen
                followDocRef.set(followData)
            }
        }
    }

    //unfollow
    fun unfollowUser(user: Users) {

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("Users").document(Utils.getUiLoggedIn())
        val userToUnfollowDocRef = firestore.collection("Users").document(user.userid!!)

        // Fetch the following count value using a get() call instead of addSnapshotListener()
        userDocRef.get().addOnSuccessListener { userSnapshot ->
            if (userSnapshot.exists()) {
                val followingCount = userSnapshot.getLong("following") ?: 0

                // Update the "followers" and "following" fields using a transaction
                firestore.runTransaction { transaction ->
                    val userToUnfollowSnapshot = transaction.get(userToUnfollowDocRef)

                    // Decrement the "followers" count of the user to unfollow
                    val followersCount = userToUnfollowSnapshot.getLong("followers") ?: 0
                    val newFollowersCount = if (followersCount > 0) followersCount - 1 else 0
                    transaction.update(userToUnfollowDocRef, "followers", newFollowersCount)

                    // Decrement the "following" count of the logged-in user
                    val newFollowingCount = if (followingCount > 0) followingCount - 1 else 0
                    transaction.update(userDocRef, "following", newFollowingCount)
                }.addOnSuccessListener {
                    // Transaction completed successfully

                    // Delete the documents in the "Follow" collection
                    firestore.collection("Follow").document(Utils.getUiLoggedIn()).get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val followingIds = documentSnapshot.get("following_id") as? List<String>
                            val newIds = followingIds?.toMutableList() ?: mutableListOf()

                            if (newIds.contains(user.userid)) {
                                newIds.remove(user.userid)
                            }

                            if (newIds.isEmpty()) {
                                // Delete the document if the list is empty
                                documentSnapshot.reference.delete()
                            } else {
                                // Update the Firestore document with the modified list
                                val followDocRef = firestore.collection("Follow").document(Utils.getUiLoggedIn())
                                followDocRef.set(hashMapOf("following_id" to newIds))
                            }
                        }
                    }






                }.addOnFailureListener { exception ->

                }
            }
        }




    }

    fun setUserList(list:List<Users>){
        this.listofusers=list


    }
    fun setListener(listener: OnFriendClicked) {

        this.listener = listener
    }
}
class UserHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
{
    val userimage: CircleImageView = itemView.findViewById(R.id.userImage_follow)
    val userName: TextView = itemView.findViewById(R.id.userName_follow)
    val toggle : SwitchCompat = itemView.findViewById(R.id.toggle)
}

interface OnFriendClicked{

    fun onfriendListener(position: Int,user: Users)
}