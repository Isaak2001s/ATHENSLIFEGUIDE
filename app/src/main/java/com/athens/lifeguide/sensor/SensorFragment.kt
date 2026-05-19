package com.athens.lifeguide.ui.sensor

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.athens.lifeguide.databinding.FragmentSensorBinding
import kotlin.math.sqrt

class SensorFragment : Fragment(), SensorEventListener {

    private var _b: FragmentSensorBinding? = null
    private val b get() = _b!!

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var accelerometer: Sensor? = null

    // Sensor values
    private var gyroX = 0f; private var gyroY = 0f; private var gyroZ = 0f
    private var accelX = 0f; private var accelY = 0f; private var accelZ = 0f

    // Motion detection
    private var lastAccelMagnitude = 0f
    private var motionSamples = mutableListOf<Float>()
    private val SAMPLE_SIZE = 20

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSensorBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Check sensor availability
        if (gyroscope == null) {
            b.tvGyroStatus.text = "⚠️ Γυροσκόπιο μη διαθέσιμο σε αυτή τη συσκευή"
            b.tvGyroStatus.setTextColor(Color.parseColor("#F44336"))
        }
        if (accelerometer == null) {
            b.tvAccelStatus.text = "⚠️ Επιταχυνσιόμετρο μη διαθέσιμο"
            b.tvAccelStatus.setTextColor(Color.parseColor("#F44336"))
        }
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
                updateGyroUI()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]
                accelY = event.values[1]
                accelZ = event.values[2]
                updateAccelUI()
                detectMotion()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateGyroUI() {
        b.tvGyroX.text = "X: ${"%.3f".format(gyroX)} rad/s"
        b.tvGyroY.text = "Y: ${"%.3f".format(gyroY)} rad/s"
        b.tvGyroZ.text = "Z: ${"%.3f".format(gyroZ)} rad/s"

        val totalRotation = sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ)
        val rotationPct = (totalRotation * 100 / 10f).coerceIn(0f, 100f).toInt()
        b.progressGyro.progress = rotationPct

        val rotColor = when {
            totalRotation < 0.5f -> "#4CAF50"
            totalRotation < 2.0f -> "#FFC107"
            else -> "#F44336"
        }
        b.tvGyroStatus.text = when {
            totalRotation < 0.5f -> "📱 Σταθερό — ελάχιστη περιστροφή"
            totalRotation < 2.0f -> "🔄 Μέτρια περιστροφή"
            else -> "🌀 Έντονη περιστροφή!"
        }
        b.tvGyroStatus.setTextColor(Color.parseColor(rotColor))
    }

    private fun updateAccelUI() {
        b.tvAccelX.text = "X: ${"%.2f".format(accelX)} m/s²"
        b.tvAccelY.text = "Y: ${"%.2f".format(accelY)} m/s²"
        b.tvAccelZ.text = "Z: ${"%.2f".format(accelZ)} m/s²"

        val magnitude = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
        b.tvAccelMagnitude.text = "Συνολική: ${"%.2f".format(magnitude)} m/s²"
    }

    private fun detectMotion() {
        val magnitude = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
        val delta = Math.abs(magnitude - lastAccelMagnitude)
        lastAccelMagnitude = magnitude

        motionSamples.add(delta)
        if (motionSamples.size > SAMPLE_SIZE) {
            motionSamples.removeAt(0)
        }

        if (motionSamples.size >= SAMPLE_SIZE) {
            val avgDelta = motionSamples.average().toFloat()

            val (status, emoji, color, suggestion) = when {
                avgDelta < 0.3f -> listOf(
                    "Ακίνητος",
                    "🧍",
                    "#4CAF50",
                    "Είστε σταθεροί. Ιδανική στιγμή να ελέγξετε τη διαθεσιμότητα στάθμευσης."
                )
                avgDelta < 1.5f -> listOf(
                    "Περπάτημα",
                    "🚶",
                    "#2196F3",
                    "Περπατάτε. Δείτε κοντινούς χώρους στάθμευσης στον χάρτη."
                )
                avgDelta < 4.0f -> listOf(
                    "Οδήγηση",
                    "🚗",
                    "#FF9800",
                    "Φαίνεται ότι οδηγείτε. Κρατήστε μια θέση τώρα πριν φτάσετε!"
                )
                else -> listOf(
                    "Γρήγορη κίνηση",
                    "🏃",
                    "#F44336",
                    "Ανιχνεύτηκε γρήγορη κίνηση. Προσοχή στην οδήγηση!"
                )
            }

            b.tvMotionEmoji.text = emoji
            b.tvMotionStatus.text = status
            b.tvMotionStatus.setTextColor(Color.parseColor(color))
            b.tvMotionSuggestion.text = suggestion

            val pct = (avgDelta * 100 / 5f).coerceIn(0f, 100f).toInt()
            b.progressMotion.progress = pct
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}