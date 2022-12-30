package com.example.practicaldemo.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.practicaldemo.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            var intent = Intent(this, LocationSearchActivity::class.java)
            startActivity(intent)
        }, 1000)

    }
}