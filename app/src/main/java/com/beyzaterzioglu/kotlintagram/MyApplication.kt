package com.beyzaterzioglu.kotlintagram

import android.app.Application

class MyApplication:Application() { // Application sınıfından türetildi.

    //bağlamı istediğimiz yerde sağlayacağımız sınıf
    //uygulamanın yaşam döngüsü boyunca yapılacak özel işlemleri bu sınıfta yapılacak
    //Application sınıfı activity ve servisler gibi bileşenleri içeren temel sınıftır.
    // Uygulamamız çalıştırıldığında ilk olarak çalışacak olan sınıf Application sınıfıdır,
    // ve istediğimiz (gerekli olduğu) durumlarda bu sınıfı customize edebilmekteyiz.
    companion object{
        lateinit var instance:Application
    }

    override fun onCreate() {
        super.onCreate()
        instance=this


    }
}