package com.apuapa.webview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION_MS = 1800L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splash_logo)
        val title = findViewById<TextView>(R.id.splash_title)
        val tagline = findViewById<TextView>(R.id.splash_tagline)
        val loading = findViewById<View>(R.id.splash_loading_container)

        // Animación de entrada: logo aparece con escala+fade
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in)
        val fadeInDelay = AnimationUtils.loadAnimation(
            this, R.anim.splash_fade_in_delay
        )
        logo.startAnimation(fadeIn)
        title.startAnimation(fadeIn)
        tagline.startAnimation(fadeInDelay)
        loading.startAnimation(fadeInDelay)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            finish()
        }, SPLASH_DURATION_MS)
    }
}
