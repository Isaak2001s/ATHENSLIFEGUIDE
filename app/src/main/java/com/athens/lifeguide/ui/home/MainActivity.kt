package com.athens.lifeguide.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.athens.lifeguide.R
import com.athens.lifeguide.data.models.Session
import com.athens.lifeguide.databinding.ActivityMainBinding
import com.athens.lifeguide.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Session.loggedIn) { goLogin(); return }

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.hide()

        val host = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        b.bottomNav.setupWithNavController(host.navController)
    }

    fun logout() {
        Session.clear()
        goLogin()
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
