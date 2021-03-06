package com.example.movieapp.ui.splash

import android.content.Intent
import android.os.Handler
import com.example.movieapp.R
import com.example.movieapp.base.BaseActivity
import com.example.movieapp.data.local.AppPreferences
import com.example.movieapp.ui.login.LoginActivity
import com.example.movieapp.ui.main.MainActivity
import com.example.movieapp.ui.main.UserActivity
import com.example.movieapp.ui.selection_type.SelectionActivity
import com.example.movieapp.utils.SPLASH_DISPLAY_LENGTH

class SplashActivity : BaseActivity() {
    private lateinit var appPreferences: AppPreferences
    override fun getLayoutID(): Int {
        return R.layout.activity_splash
    }

    override fun doViewCreated() {
        appPreferences = AppPreferences(this)
        openNewScreen()
    }

    private fun openNewScreen() {
        if (appPreferences.getIsLogin()) {
            if (appPreferences.getLoginEmail()?.contains("admin") == true) {
                Handler().postDelayed({
                    val intentNewScreen = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intentNewScreen)
                    finish()
                }, SPLASH_DISPLAY_LENGTH)
            } else {
                Handler().postDelayed({
                    val intentNewScreen = Intent(this@SplashActivity, UserActivity::class.java)
                    startActivity(intentNewScreen)
                    finish()
                }, SPLASH_DISPLAY_LENGTH)
            }

        } else {
            Handler().postDelayed({
                val intentNewScreen = Intent(this@SplashActivity, SelectionActivity::class.java)
                startActivity(intentNewScreen)
                finish()
            }, SPLASH_DISPLAY_LENGTH)
        }

    }
}