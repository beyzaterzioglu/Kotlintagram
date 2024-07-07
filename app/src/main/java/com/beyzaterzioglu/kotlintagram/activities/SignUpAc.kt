@file:Suppress("DEPRECATION")

package com.beyzaterzioglu.kotlintagram.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.databinding.ActivitySignInBinding
import com.beyzaterzioglu.kotlintagram.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpAc : AppCompatActivity() {
    //kayıt olma aktivitesi

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private  lateinit var pd: ProgressDialog
    private lateinit var firestore: FirebaseFirestore // kullanıcıları burada depolar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        binding= DataBindingUtil.setContentView(this,R.layout.activity_sign_up)
        auth=FirebaseAuth.getInstance()
        firestore=FirebaseFirestore.getInstance()
        pd= ProgressDialog(this)
        binding.signUpTexttoSignIn.setOnClickListener{
            startActivity(Intent(this,SignInAc::class.java))

        }
        binding.signUpButton.setOnClickListener {
            //kayıt ol butonuna basıldığında
            if(binding.signUpnetemail.text.isNotEmpty()&&binding.signUpetpassword.text.isNotEmpty())
            {
                val email=binding.signUpnetemail.text.toString()
                val password=binding.signUpetpassword.text.toString()
                val name=binding.signUpName.text.toString()
                signUp(name,email,password)
            }
            if(binding.signUpnetemail.text.isEmpty() || binding.signUpetpassword.text.isEmpty() || binding.signUpName.text.isEmpty())
            {

                Toast.makeText(this,"Devam etmek için boşlukları doldurunuz.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun signUp(name:String,email:String,password:String)
    {
        pd.show()
        pd.setMessage("Kayıt yapılıyor..")
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task->
            if(task.isSuccessful)
            {
                val user=auth.currentUser
                //varsayılan değerler
                val hashMap= hashMapOf("userid" to user!!.uid,"username" to name,"image" to "https://firebasestorage.googleapis.com/v0/b/kotlintagram.appspot.com/o/Photos%2Fblank-profile-picture-973460_960_720.webp?alt=media&token=ef89a47e-357a-4376-8e11-c58e206ce98f", //default resim link eklencek buraya,
                    "email" to email,"followers" to 0,"following" to 0 )

                firestore.collection("Users").document(user.uid).set(hashMap)
                pd.dismiss()
                startActivity(Intent(this,SignInAc::class.java))


            }
        }
    }
}