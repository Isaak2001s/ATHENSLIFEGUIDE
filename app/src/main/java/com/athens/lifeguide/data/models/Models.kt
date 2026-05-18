package com.athens.lifeguide.data.models

import android.graphics.Color

// ══════════════════════════════════════════════════
//  PLACE
// ══════════════════════════════════════════════════

enum class PlaceType(val label: String, val emoji: String, val colorHex: String) {
    PARK    ("Πάρκο",           "🌳", "#2E7D32"),
    SQUARE  ("Πλατεία",         "⛲", "#1565C0"),
    METRO   ("Μετρό",           "🚇", "#6A1B9A"),
    TRAM    ("Τραμ",            "🚊", "#E65100"),
    BUS     ("Λεωφορείο",       "🚌", "#00695C"),
    AQI     ("Σταθμός Αέρα",    "💨", "#37474F")
}

data class Place(
    val id: String,
    val name: String,
    val type: PlaceType,
    val lat: Double,
    val lng: Double,
    val description: String = "",
    val lines: List<String> = emptyList(),
    var isFavorite: Boolean = false
) {
    fun typeColor(): Int = Color.parseColor(type.colorHex)
}

// ══════════════════════════════════════════════════
//  AQI
// ══════════════════════════════════════════════════

enum class AqiLevel(
    val labelGr: String,
    val colorHex: String,
    val emoji: String
) {
    GOOD                ("Καλή",                        "#00C853", "😊"),
    MODERATE            ("Μέτρια",                      "#FFD600", "🙂"),
    UNHEALTHY_SENSITIVE ("Ανθυγιεινή (Ευαίσθητες)",    "#FF6D00", "😐"),
    UNHEALTHY           ("Ανθυγιεινή",                  "#DD2C00", "😷"),
    VERY_UNHEALTHY      ("Πολύ Ανθυγιεινή",             "#6A1B9A", "🤢"),
    HAZARDOUS           ("Επικίνδυνη",                  "#B71C1C", "☠️"),
    UNKNOWN             ("Άγνωστη",                     "#607D8B", "❓");

    fun color(): Int = Color.parseColor(colorHex)

    companion object {
        fun from(aqi: Int): AqiLevel = when {
            aqi < 0   -> UNKNOWN
            aqi <= 50  -> GOOD
            aqi <= 100 -> MODERATE
            aqi <= 150 -> UNHEALTHY_SENSITIVE
            aqi <= 200 -> UNHEALTHY
            aqi <= 300 -> VERY_UNHEALTHY
            else       -> HAZARDOUS
        }
    }
}

data class AqiStation(
    val uid: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val aqi: Int,
    val level: AqiLevel = AqiLevel.from(aqi),
    val pm25: Double? = null,
    val pm10: Double? = null,
    val o3:   Double? = null,
    val no2:  Double? = null,
    val updatedAt: String? = null
)

// ══════════════════════════════════════════════════
//  SESSION
// ══════════════════════════════════════════════════

object Session {
    var userId: Int = -1
    var username: String = ""
    val loggedIn get() = userId != -1

    fun start(id: Int, name: String) { userId = id; username = name }
    fun clear() { userId = -1; username = "" }
}
// ══════════════════════════════════════════════════
//  PARKING
// ══════════════════════════════════════════════════

data class ParkingSpot(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val totalSpots: Int,
    val availableSpots: Int,
    val pricePerHour: Double,
    val address: String,
    val occupancyLevel: String = "low"
) {
    val hasAvailability: Boolean get() = availableSpots > 0

    fun occupancyColor(): Int = Color.parseColor(
        when (occupancyLevel) {
            "low" -> "#4CAF50"
            "medium" -> "#FFC107"
            "high" -> "#F44336"
            else -> "#607D8B"
        }
    )
}
