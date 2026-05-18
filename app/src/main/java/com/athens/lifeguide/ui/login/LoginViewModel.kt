package com.athens.lifeguide.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.athens.lifeguide.BuildConfig
import com.athens.lifeguide.data.db.AppDatabase
import com.athens.lifeguide.data.db.UserEntity
import com.athens.lifeguide.data.repository.AppRepository
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: UserEntity) : LoginUiState()
    data class Error(val msg: String) : LoginUiState()
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(
        AppDatabase.get(app),
        BuildConfig.AQICN_TOKEN
    )

    val state = MutableLiveData<LoginUiState>(LoginUiState.Idle)

    fun login(username: String, password: String) {
        if (!validate(username, password)) return
        state.value = LoginUiState.Loading
        viewModelScope.launch {
            state.value = repo.login(username, password).fold(
                onSuccess = { LoginUiState.Success(it) },
                onFailure = { LoginUiState.Error(it.message ?: "Σφάλμα") }
            )
        }
    }

    fun register(username: String, password: String, confirm: String) {
        if (!validate(username, password)) return
        if (password != confirm) { state.value = LoginUiState.Error("Οι κωδικοί δεν ταιριάζουν"); return }
        if (password.length < 6) { state.value = LoginUiState.Error("Κωδικός: τουλάχιστον 6 χαρακτήρες"); return }
        state.value = LoginUiState.Loading
        viewModelScope.launch {
            state.value = repo.register(username, password).fold(
                onSuccess = { LoginUiState.Success(it) },
                onFailure = { LoginUiState.Error(it.message ?: "Σφάλμα") }
            )
        }
    }

    private fun validate(u: String, p: String): Boolean {
        if (u.isBlank() || p.isBlank()) {
            state.value = LoginUiState.Error("Συμπλήρωσε όλα τα πεδία")
            return false
        }
        return true
    }
}
