package com.athens.lifeguide.utils

import android.location.Location

object LocationUtils {
    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val r = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, r)
        return r[0]
    }
    fun fmt(m: Float) = if (m < 1000) "${m.toInt()} μ" else "${"%.1f".format(m / 1000)} χλμ"
}

object AqiAdvice {
    fun get(aqi: Int) = when {
        aqi <= 50  -> "Εξαιρετική ποιότητα αέρα! Ιδανικό για εξωτερικές δραστηριότητες. 🏃"
        aqi <= 100 -> "Αποδεκτή ποιότητα. Τα ευαίσθητα άτομα να είναι λίγο προσεκτικά. 🚶"
        aqi <= 150 -> "Ανθυγιεινό για ευαίσθητες ομάδες. Περιορίστε έντονη άσκηση εξωτερικά. ⚠️"
        aqi <= 200 -> "Ανθυγιεινός αέρας για όλους. Αποφύγετε παρατεταμένη έκθεση. 😷"
        aqi <= 300 -> "Πολύ ανθυγιεινό. Παραμείνετε στο εσωτερικό αν είναι δυνατόν. 🚨"
        else       -> "Επικίνδυνα επίπεδα! Αποφύγετε εντελώς οποιαδήποτε εξωτερική δραστηριότητα. ☠️"
    }
}
