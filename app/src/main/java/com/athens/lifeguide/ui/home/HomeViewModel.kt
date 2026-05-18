package com.athens.lifeguide.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.athens.lifeguide.BuildConfig
import com.athens.lifeguide.data.db.AppDatabase
import com.athens.lifeguide.data.models.AqiStation
import com.athens.lifeguide.data.repository.AppRepository
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(AppDatabase.get(app), BuildConfig.AQICN_TOKEN)

    val aqiResult  = MutableLiveData<Result<AqiStation>>()
    val isRefreshing = MutableLiveData(false)

    init { refresh() }

    fun refresh() {
        isRefreshing.value = true
        viewModelScope.launch {
            aqiResult.value = repo.getAthensAqi()
            isRefreshing.value = false
        }
    }
}
