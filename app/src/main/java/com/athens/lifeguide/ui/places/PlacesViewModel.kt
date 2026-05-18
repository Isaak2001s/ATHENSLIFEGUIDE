package com.athens.lifeguide.ui.places

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.athens.lifeguide.BuildConfig
import com.athens.lifeguide.data.db.AppDatabase
import com.athens.lifeguide.data.db.FavoriteEntity
import com.athens.lifeguide.data.models.AthensData
import com.athens.lifeguide.data.models.Place
import com.athens.lifeguide.data.models.Session
import com.athens.lifeguide.data.repository.AppRepository
import kotlinx.coroutines.launch

enum class PlacesTab { PARKS, SQUARES, TRANSIT }

class PlacesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(AppDatabase.get(app), BuildConfig.AQICN_TOKEN)

    val currentTab = MutableLiveData(PlacesTab.PARKS)
    val places     = MutableLiveData<List<Place>>(emptyList())
    val toastMsg   = MutableLiveData<String?>()

    init { loadTab(PlacesTab.PARKS) }

    fun switchTab(tab: PlacesTab) {
        currentTab.value = tab
        loadTab(tab)
    }

    private fun loadTab(tab: PlacesTab) {
        viewModelScope.launch {
            val raw = when (tab) {
                PlacesTab.PARKS   -> AthensData.parks
                PlacesTab.SQUARES -> AthensData.squares
                PlacesTab.TRANSIT -> AthensData.transit
            }
            val favIds = repo.getFavorites(Session.userId).map { it.placeId }.toSet()
            places.value = raw.map { it.copy(isFavorite = it.id in favIds) }
        }
    }

    fun toggleFavorite(place: Place) {
        viewModelScope.launch {
            if (place.isFavorite) {
                repo.removeFavorite(Session.userId, place.id)
                toastMsg.value = "Αφαιρέθηκε από αγαπημένα"
            } else {
                repo.addFavorite(FavoriteEntity(
                    userId      = Session.userId,
                    placeId     = place.id,
                    placeName   = place.name,
                    placeType   = place.type.name.lowercase(),
                    latitude    = place.lat,
                    longitude   = place.lng,
                    description = place.description
                ))
                toastMsg.value = "⭐ «${place.name}» προστέθηκε στα αγαπημένα!"
            }
            loadTab(currentTab.value ?: PlacesTab.PARKS)
        }
    }
}
