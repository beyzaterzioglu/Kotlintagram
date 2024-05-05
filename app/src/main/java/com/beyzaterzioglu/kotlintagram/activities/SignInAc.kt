package com.beyzaterzioglu.kotlintagram.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.beyzaterzioglu.kotlintagram.MainActivity
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth


class SignInAc : AppCompatActivity() {

    private lateinit var binding:ActivitySignInBinding
    private lateinit var auth:FirebaseAuth
    private  lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_sign_in)
        auth=FirebaseAuth.getInstance()
        binding.signInTextToSignUp.setOnClickListener{
            startActivity(Intent(this,SignUpAc::class.java))

        }

        if(auth.currentUser!=null)
        {//eğer kullanıcı daha önce giriş yaptıysa beni main activity'e gönderir.
            startActivity((Intent(this,MainActivity::class.java)))
        }
        pd= ProgressDialog(this)

        binding.loginButton.setOnClickListener {
            if(binding.loginetemail.text.isNotEmpty()&&binding.loginetpassword.text.isNotEmpty())
            {
                //şifre ve email alanlarını kontrol eder
                // boş değilse girdileri kontrol için değişkene aktarır.
                val email=binding.loginetemail.text.toString()
                val password=binding.loginetpassword.text.toString()

                signIn(email,password)
            }
            if(binding.loginetemail.text.isNotEmpty() || binding.loginetpassword.text.isNotEmpty())
            {
                //Herhangi bir alan boş ise
                Toast.makeText(this,"Boş alan bırakamazsınız!",Toast.LENGTH_SHORT).show()


            }
        }



    }
    private fun signIn(email:String,password:String)
    {
     //giriş işleminin yapıldığı fonksiyon
        pd.show()
        pd.setMessage("Giriş yapılıyor..")
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful)
            {
                //eğer mail ve password doğru ise
                startActivity((Intent(this,MainActivity::class.java)))
                Toast.makeText(this,"Giriş başarıyla yapıldı.",Toast.LENGTH_SHORT).show()
                pd.dismiss()
            }
        }.addOnFailureListener {
            //neden giriş yapılamadığıyla ilgili bilgilendirme mesajı verir.
            pd.dismiss()
            Toast.makeText(this,it.toString(),Toast.LENGTH_SHORT).show()
            return@addOnFailureListener


        }
    }
}