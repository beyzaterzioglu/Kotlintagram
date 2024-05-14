package com.beyzaterzioglu.kotlintagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var navController:NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if(supportFragmentManager.backStackEntryCount>0)
        {
            super.onBackPressed()
        }
        else
        {
            if(navController.currentDestination?.id== R.id.profileFrag){
                moveTaskToBack(true)
            } else
                super.onBackPressed()
        }
    }
}