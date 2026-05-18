package com.athens.lifeguide.data.repository

import com.athens.lifeguide.data.api.AqiClient
import com.athens.lifeguide.data.db.AppDatabase
import com.athens.lifeguide.data.db.FavoriteEntity
import com.athens.lifeguide.data.db.UserEntity
import com.athens.lifeguide.data.models.AqiLevel
import com.athens.lifeguide.data.models.AqiStation
import java.security.MessageDigest
import com.athens.lifeguide.data.db.ParkingAreaEntity
import com.athens.lifeguide.data.db.ReservationEntity
import com.athens.lifeguide.data.models.AthensData

class AppRepository(private val db: AppDatabase, private val aqiToken: String) {

    // ── Auth ──────────────────────────────────────────────────────────────

    suspend fun register(username: String, password: String): Result<UserEntity> {
        return runCatching {
            val existing = db.userDao().getByUsername(username)
            require(existing == null) { "Το username χρησιμοποιείται ήδη" }

            val user = UserEntity(username = username, passwordHash = sha256(password))
            val rowId = db.userDao().insertUser(user)
            require(rowId > 0) { "Αποτυχία εγγραφής" }
            user.copy(id = rowId.toInt())
        }
    }

    suspend fun login(username: String, password: String): Result<UserEntity> {
        return runCatching {
            val user = db.userDao().login(username, sha256(password))
            requireNotNull(user) { "Λάθος username ή password" }
            user
        }
    }

    // ── Favorites ─────────────────────────────────────────────────────────

    suspend fun getFavorites(userId: Int) =
        db.favoriteDao().getAll(userId)

    suspend fun addFavorite(fav: FavoriteEntity): Boolean =
        db.favoriteDao().insert(fav) > 0

    suspend fun removeFavorite(userId: Int, placeId: String) =
        db.favoriteDao().delete(userId, placeId)

    suspend fun isFavorite(userId: Int, placeId: String) =
        db.favoriteDao().count(userId, placeId) > 0

    // ── AQI ───────────────────────────────────────────────────────────────

    suspend fun getAthensAqi(): Result<AqiStation> = runCatching {
        val resp = AqiClient.service.feedByCity("athens", aqiToken)
        check(resp.status == "ok" && resp.data != null) { "AQI API error: ${resp.status}" }
        val d = resp.data!!
        AqiStation(
            uid       = d.idx,
            name      = d.city.name,
            lat       = d.city.geo?.getOrNull(0) ?: 37.9838,
            lng       = d.city.geo?.getOrNull(1) ?: 23.7275,
            aqi       = d.aqi,
            level     = AqiLevel.from(d.aqi),
            pm25      = d.iaqi?.pm25?.v,
            pm10      = d.iaqi?.pm10?.v,
            o3        = d.iaqi?.o3?.v,
            no2       = d.iaqi?.no2?.v,
            updatedAt = d.time?.s
        )
    }

    suspend fun getStationsInBounds(
        lat1: Double, lng1: Double, lat2: Double, lng2: Double
    ): Result<List<AqiStation>> = runCatching {
        val resp = AqiClient.service.stationsInBounds("$lat1,$lng1,$lat2,$lng2", aqiToken)
        check(resp.status == "ok") { "Bounds API error" }
        resp.data.orEmpty().mapNotNull { s ->
            val aqiInt = s.aqi.toIntOrNull() ?: return@mapNotNull null
            val geo    = s.station.geo ?: return@mapNotNull null
            AqiStation(
                uid   = s.uid,
                name  = s.station.name,
                lat   = geo.getOrNull(0) ?: return@mapNotNull null,
                lng   = geo.getOrNull(1) ?: return@mapNotNull null,
                aqi   = aqiInt,
                level = AqiLevel.from(aqiInt)
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }



    // ── Parking ───────────────────────────────────────────────────────────

    suspend fun seedParkingIfEmpty() {
        val existing = db.parkingDao().getAll()
        if (existing.isEmpty()) {
            val entities = AthensData.parking.map {
                ParkingAreaEntity(
                    id = it.id, name = it.name,
                    latitude = it.lat, longitude = it.lng,
                    totalSpots = it.totalSpots, availableSpots = it.availableSpots,
                    pricePerHour = it.pricePerHour, address = it.address,
                    occupancyLevel = it.occupancyLevel
                )
            }
            db.parkingDao().insertAll(entities)
        }
    }

    suspend fun getParkingAreas(): List<ParkingAreaEntity> =
        db.parkingDao().getAll()

    suspend fun reserveSpot(userId: Int, area: ParkingAreaEntity): Result<ReservationEntity> {
        return runCatching {
            val fresh = db.parkingDao().getById(area.id)
                ?: throw IllegalStateException("Χώρος δεν βρέθηκε")

            require(fresh.availableSpots > 0) { "Δεν υπάρχουν διαθέσιμες θέσεις" }

            // Calculate new occupancy level
            val newAvailable = fresh.availableSpots - 1
            val ratio = (fresh.totalSpots - newAvailable).toFloat() / fresh.totalSpots
            val newLevel = when {
                ratio >= 0.8f -> "high"
                ratio >= 0.5f -> "medium"
                else -> "low"
            }

            val updated = db.parkingDao().decrementSpot(area.id, newLevel)
            require(updated > 0) { "Η κράτηση απέτυχε — δοκιμάστε ξανά" }

            val reservation = ReservationEntity(
                userId = userId,
                parkingAreaId = area.id,
                parkingAreaName = area.name,
                pricePerHour = area.pricePerHour
            )
            val rowId = db.reservationDao().insert(reservation)
            reservation.copy(id = rowId.toInt())
        }
    }

    suspend fun cancelReservation(reservation: ReservationEntity): Result<Unit> {
        return runCatching {
            db.reservationDao().cancel(reservation.id)

            val area = db.parkingDao().getById(reservation.parkingAreaId) ?: return@runCatching
            val newAvailable = area.availableSpots + 1
            val ratio = (area.totalSpots - newAvailable).toFloat() / area.totalSpots
            val newLevel = when {
                ratio >= 0.8f -> "high"
                ratio >= 0.5f -> "medium"
                else -> "low"
            }
            db.parkingDao().incrementSpot(reservation.parkingAreaId, newLevel)
        }
    }

    suspend fun getUserReservations(userId: Int): List<ReservationEntity> =
        db.reservationDao().getByUser(userId)

}
