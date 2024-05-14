package com.beyzaterzioglu.kotlintagram.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beyzaterzioglu.kotlintagram.Utils
import com.beyzaterzioglu.kotlintagram.modal.Feed
import com.beyzaterzioglu.kotlintagram.modal.Posts
import com.beyzaterzioglu.kotlintagram.modal.Users
import com.google.common.collect.Lists
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel:ViewModel() {


    val name = MutableLiveData<String>()
    val image = MutableLiveData<String>()
    val followers = MutableLiveData<String>()
    val following = MutableLiveData<String>()

    init {
        //viewmodel oluşunca fonskiyonu çağırır
        getCurrentUser()
    }
fun getCurrentUser()=viewModelScope.launch(Dispatchers.IO) {
    // Kullanıcı verilerini almak için Firestore'dan sorgu yapma işlemi
    val firestore=FirebaseFirestore.getInstance()
    firestore.collection("Users").document(Utils.getUiLoggedIn()).addSnapshotListener{
        value,error ->
        if(error!= null){
            return@addSnapshotListener
        }
        // Kullanıcı belgesi varsa, verileri Users sınıfından al
        if (value!=null && value!!.exists()){
            val users= value.toObject(Users:: class.java)
            name.value=users!!.username!!
            image.value=users.image!!
            followers.value=users.followers!!.toString()
            following.value=users.following!!.toString()

        }
    }
}
    // Kullanıcının gönderilerini almak için Firestore'dan sorgu yapma işlemi

    fun getMyPost(): LiveData<List<Posts>> {
        val posts = MutableLiveData<List<Posts>>()
        val firestore = FirebaseFirestore.getInstance()

        //background thread

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("Posts")
                    .whereEqualTo("userid", Utils.getUiLoggedIn())
                    .addSnapshotListener { snapshot, exception ->
                        if (exception != null) {

                            // addSnapshotListener, Firebase Firestore veritabanındaki belirli bir
                            // sorgunun sonuçlarındaki herhangi bir değişikliği dinlemek için kullanılan bir yöntemdir.
                            // Bu yöntem, belirtilen sorgunun sonucunda herhangi bir belge eklendiğinde, değiştirildiğinde
                            // veya silindiğinde tetiklenir.
                            return@addSnapshotListener
                        }

                        // Snapshot null değilse ve document varsa, postList'i oluştur
                        val postList = snapshot?.documents?.mapNotNull {
                            it.toObject(Posts::class.java)
                        }

                            // ne son posu gösterir.
                            ?.sortedByDescending { it.time }
                        posts.postValue(postList!!)
                    }
            } catch (e: Exception) {
                // Hata durumunu işle
            }
        }

        // LiveData'nın sonucunu döndür
        return posts
    }

    fun getAllUsers():LiveData<List<Users>>{

        val users= MutableLiveData<List<Users>>()
        val firestore=FirebaseFirestore.getInstance()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("Users").addSnapshotListener{value,error ->
                    if(error!=null){
                        return@addSnapshotListener
                    }
                    val userList= mutableListOf<Users>()
                    value?.documents?.forEach { document ->
                        val user = document.toObject(Users::class.java)

                        if(user!=null && user.userid!=Utils.getUiLoggedIn())
                        {
                            //user boş değilse ve girişi yapılmış kişi değilse göster
                            userList.add(user)
                        }
                    }
                    users.postValue(userList)
                }
            }
            catch (e: Exception){


            }
        }

        return users
    }

    fun loadMyFeed():LiveData<List<Feed>>{
        val firestore=FirebaseFirestore.getInstance()
        val feeds=MutableLiveData<List<Feed>>()
        viewModelScope.launch(Dispatchers.IO){


               getThePeopleIfollow { list ->

                   try {
                       firestore.collection("Posts").whereIn("userid",list).addSnapshotListener{
                           value,error ->
                           if(error != null)
                           {
                               return@addSnapshotListener
                           }
                           val feed= mutableListOf<Feed>()
                           value?.documents?.forEach{
                               docsnaps ->
                               val pmodal=docsnaps.toObject(Feed::class.java)
                               pmodal?.let {
                                   feed.add(it)
                               }
                           }
                           val sortedFeed=feed.sortedByDescending { it.time }
                           feeds.postValue(sortedFeed)
                       }

                   }
                   catch (e :Exception)
                   {

                   }





            }
        }
        return feeds
    }

    fun getThePeopleIfollow(callback:(List<String>)-> Unit){
        val firestore=FirebaseFirestore.getInstance()
        val ifollowlist= mutableListOf<String>()

        ifollowlist.add(Utils.getUiLoggedIn())

        firestore.collection("Follow").document(Utils.getUiLoggedIn()).get().addOnSuccessListener {

            docsnap->
            if(docsnap.exists())
               {
                val followingids=docsnap.get("following_id") as? List<String>
                   val updateList=followingids?.toMutableList()?: mutableListOf()

                   ifollowlist.addAll(updateList)

                   callback(ifollowlist)

                 }
            else
            {
                callback(ifollowlist)
            }
        }
    }
}