package com.athens.lifeguide.ui.parking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.athens.lifeguide.BuildConfig
import com.athens.lifeguide.data.db.AppDatabase
import com.athens.lifeguide.data.db.ParkingAreaEntity
import com.athens.lifeguide.data.db.ReservationEntity
import com.athens.lifeguide.data.models.Session
import com.athens.lifeguide.data.repository.AppRepository
import kotlinx.coroutines.launch

sealed class ParkingUiState {
    object Loading : ParkingUiState()
    data class Ready(
        val areas: List<ParkingAreaEntity>,
        val reservations: List<ReservationEntity>
    ) : ParkingUiState()
    data class Error(val msg: String) : ParkingUiState()
}

class ParkingViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(AppDatabase.get(app), BuildConfig.AQICN_TOKEN)

    val state = MutableLiveData<ParkingUiState>(ParkingUiState.Loading)
    val toastMsg = MutableLiveData<String?>()

    init { load() }

    fun load() {
        viewModelScope.launch {
            state.value = ParkingUiState.Loading
            try {
                repo.seedParkingIfEmpty()
                val areas = repo.getParkingAreas()
                val reservations = repo.getUserReservations(Session.userId)
                state.value = ParkingUiState.Ready(areas, reservations)
            } catch (e: Exception) {
                state.value = ParkingUiState.Error(e.message ?: "Σφάλμα φόρτωσης")
            }
        }
    }

    fun reserve(area: ParkingAreaEntity) {
        viewModelScope.launch {
            repo.reserveSpot(Session.userId, area)
                .onSuccess {
                    toastMsg.value = "✅ Κράτηση στο «${area.name}» επιτυχής!"
                    load()
                }
                .onFailure {
                    toastMsg.value = "❌ ${it.message}"
                }
        }
    }

    fun cancelReservation(reservation: ReservationEntity) {
        viewModelScope.launch {
            repo.cancelReservation(reservation)
                .onSuccess {
                    toastMsg.value = "Η κράτηση ακυρώθηκε"
                    load()
                }
                .onFailure {
                    toastMsg.value = "❌ ${it.message}"
                }
        }
    }
}