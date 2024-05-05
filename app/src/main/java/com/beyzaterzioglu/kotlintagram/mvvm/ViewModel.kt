package com.beyzaterzioglu.kotlintagram.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beyzaterzioglu.kotlintagram.Utils
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
}