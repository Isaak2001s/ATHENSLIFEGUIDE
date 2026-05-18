package com.athens.lifeguide.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.athens.lifeguide.BuildConfig
import com.athens.lifeguide.data.db.AppDatabase
import com.athens.lifeguide.data.db.FavoriteEntity
import com.athens.lifeguide.data.models.AqiStation
import com.athens.lifeguide.data.models.Session
import com.athens.lifeguide.data.repository.AppRepository
import kotlinx.coroutines.launch
import com.athens.lifeguide.data.db.ParkingAreaEntity
import com.athens.lifeguide.data.db.ReservationEntity


class MapViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(AppDatabase.get(app), BuildConfig.AQICN_TOKEN)

    val aqiStations = MutableLiveData<List<AqiStation>>(emptyList())
    val saveResult  = MutableLiveData<String?>()

    init { loadAqi() }

    private fun loadAqi() {
        viewModelScope.launch {
            // Try bounds for Athens
            repo.getStationsInBounds(37.85, 23.60, 38.10, 23.90)
                .onSuccess  { aqiStations.value = it }
                .onFailure  {
                    // Fallback: single city feed
                    repo.getAthensAqi().onSuccess { aqiStations.value = listOf(it) }
                }
        }
    }

    fun saveFavorite(
        placeId: String, name: String, type: String,
        lat: Double, lng: Double, desc: String
    ) {
        viewModelScope.launch {
            val fav = FavoriteEntity(
                userId = Session.userId,
                placeId = placeId, placeName = name, placeType = type,
                latitude = lat, longitude = lng, description = desc
            )
            val ok = repo.addFavorite(fav)
            saveResult.value = if (ok) "⭐ «$name» προστέθηκε στα αγαπημένα!" else null
        }
    }
    val reserveResult = MutableLiveData<String?>()

    fun reserveParking(parkingId: String, parkingName: String, price: Double) {
        viewModelScope.launch {
            val area = repo.getParkingAreas().find { it.id == parkingId }
            if (area == null) {
                reserveResult.value = "❌ Χώρος δεν βρέθηκε"
                return@launch
            }
            repo.reserveSpot(Session.userId, area)
                .onSuccess { reserveResult.value = "✅ Κράτηση στο «$parkingName» επιτυχής!" }
                .onFailure { reserveResult.value = "❌ ${it.message}" }
        }
    }
    fun refreshParkingFromDb() {
        viewModelScope.launch {
            val areas = repo.getParkingAreas()
            val arr = areas.map {
                mapOf(
                    "id" to it.id,
                    "name" to it.name,
                    "lat" to it.latitude,
                    "lng" to it.longitude,
                    "total" to it.totalSpots,
                    "available" to it.availableSpots,
                    "price" to it.pricePerHour,
                    "address" to it.address,
                    "occupancyLevel" to it.occupancyLevel
                )
            }
            parkingData.value = arr
        }
    }

    val parkingData = MutableLiveData<List<Map<String, Any>>>(emptyList())
}
