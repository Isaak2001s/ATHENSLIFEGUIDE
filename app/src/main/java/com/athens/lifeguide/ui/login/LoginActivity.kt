package com.athens.lifeguide.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.athens.lifeguide.data.models.Session
import com.athens.lifeguide.databinding.ActivityLoginBinding
import com.athens.lifeguide.ui.home.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private val vm: LoginViewModel by viewModels()
    private var isRegister = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.hide()

        // Already logged in?
        if (Session.loggedIn) { goMain(); return }

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        b.btnPrimary.setOnClickListener {
            val u = b.etUsername.text.toString().trim()
            val p = b.etPassword.text.toString()
            if (isRegister) vm.register(u, p, b.etConfirm.text.toString())
            else             vm.login(u, p)
        }

        b.tvToggle.setOnClickListener {
            isRegister = !isRegister
            b.tilConfirm.visibility = if (isRegister) View.VISIBLE else View.GONE
            b.btnPrimary.text       = if (isRegister) "Εγγραφή" else "Σύνδεση"
            b.tvTitle.text          = if (isRegister) "Δημιουργία λογαριασμού" else "Σύνδεση"
            b.tvToggle.text         = if (isRegister) "Έχεις λογαριασμό; Σύνδεση" else "Δεν έχεις λογαριασμό; Εγγραφή"
        }
    }

    private fun observeState() {
        vm.state.observe(this) { state ->
            when (state) {
                is LoginUiState.Idle    -> setLoading(false)
                is LoginUiState.Loading -> setLoading(true)
                is LoginUiState.Success -> {
                    setLoading(false)
                    Session.start(state.user.id, state.user.username)
                    goMain()
                }
                is LoginUiState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, state.msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(on: Boolean) {
        b.progressBar.visibility = if (on) View.VISIBLE else View.GONE
        b.btnPrimary.isEnabled   = !on
        b.tvToggle.isEnabled     = !on
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
