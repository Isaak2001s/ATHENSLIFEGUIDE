package com.athens.lifeguide.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.athens.lifeguide.BuildConfig
import com.athens.lifeguide.data.db.AppDatabase
import com.athens.lifeguide.data.db.FavoriteEntity
import com.athens.lifeguide.data.models.Session
import com.athens.lifeguide.data.repository.AppRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(AppDatabase.get(app), BuildConfig.AQICN_TOKEN)

    val favorites  = MutableLiveData<List<FavoriteEntity>>(emptyList())
    val toastMsg   = MutableLiveData<String?>()

    fun load() {
        viewModelScope.launch {
            favorites.value = repo.getFavorites(Session.userId)
        }
    }

    fun remove(fav: FavoriteEntity) {
        viewModelScope.launch {
            repo.removeFavorite(Session.userId, fav.placeId)
            toastMsg.value = "«${fav.placeName}» αφαιρέθηκε"
            load()
        }
    }
}
