package com.athens.lifeguide.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.athens.lifeguide.data.models.AthensData
import com.athens.lifeguide.databinding.FragmentMapBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject



class MapFragment : Fragment() {

    private var _b: FragmentMapBinding? = null
    private val b get() = _b!!
    private val vm: MapViewModel by viewModels()
    private var mapReady = false

    // Compass/Gyro
    private var isGyroLocked = false
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    // Location
    private val locationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) fetchLocation()
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMapBinding.inflate(i, c, false); return b.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        setupWebView()
        setupFilters()
        setupMyLocation()
        setupGyroLock()
        observeViewModel()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        b.webMap.settings.apply {
            javaScriptEnabled      = true
            domStorageEnabled      = true
            allowFileAccess        = true
            allowContentAccess     = true
            mixedContentMode       = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort        = true
            loadWithOverviewMode   = true
            builtInZoomControls    = false
            displayZoomControls    = false
        }

        b.webMap.addJavascriptInterface(JsBridge(), "Android")
        b.webMap.webChromeClient = WebChromeClient()
        b.webMap.webViewClient   = object : WebViewClient() {
            override fun onReceivedError(v: WebView?, r: WebResourceRequest?, e: WebResourceError?) {
                // suppress — offline errors handled gracefully
            }
        }

        // Load HTML from raw resource
        val html = requireContext().resources.openRawResource(
            requireContext().resources.getIdentifier("map", "raw", requireContext().packageName)
        ).bufferedReader().use { it.readText() }

        b.webMap.loadDataWithBaseURL(
            "https://maps.geoapify.com/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun setupFilters() {
        val chips = mapOf(
            b.chipAll     to "all",
            b.chipParks   to "parks",
            b.chipSquares to "squares",
            b.chipTransit to "transit",
            b.chipParking to "parking",
            b.chipTraffic to "traffic"
        )
        chips.forEach { (chip, filter) ->
            chip.setOnClickListener {
                chips.keys.forEach { c ->
                    c.isSelected = false
                    c.isChecked = false
                }
                chip.isSelected = true
                chip.isChecked = true
                evalJs("setFilter('$filter')")
            }
        }
        b.chipAll.isSelected = true
        b.chipAll.isChecked = true
    }

    private fun setupMyLocation() {
        b.fabLocation.setOnClickListener { checkAndFetchLocation() }
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                
                // Remap the coordinates so it works correctly if the phone is held up in portrait mode
                val remappedMatrix = FloatArray(9)
                SensorManager.remapCoordinateSystem(rotationMatrix, 
                    SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedMatrix)

                val orientation = FloatArray(3)
                SensorManager.getOrientation(remappedMatrix, orientation)
                
                var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f
                
                // Always update the cone rotation
                evalJs("updateMyHeading($azimuthDeg)")
                
                // If map rotation is locked, rotate the whole map
                if (isGyroLocked) {
                    evalJs("setCompassRotation($azimuthDeg)")
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun setupGyroLock() {
        b.fabGyroLock.setOnClickListener {
            isGyroLocked = !isGyroLocked
            if (isGyroLocked) {
                b.fabGyroLock.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GREEN)
                evalJs("setCompassLock(true)")
            } else {
                b.fabGyroLock.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                evalJs("setCompassLock(false)")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
        LocationServices.getFusedLocationProviderClient(requireActivity()).removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestContinuousLocation()
        }
    }

    private fun observeViewModel() {
        vm.aqiStations.observe(viewLifecycleOwner) { stations ->
            if (mapReady && stations.isNotEmpty()) pushAqiToMap(stations)
        }
        vm.saveResult.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
        vm.reserveResult.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                vm.reserveResult.value = null
                vm.refreshParkingFromDb()
            }
        }
        vm.parkingData.observe(viewLifecycleOwner) { data ->
            if (mapReady && data.isNotEmpty()) pushParkingToMap()
        }
    }

    // ── Called from JS when Leaflet is ready ──────────────────────────────
    private fun onMapReady() {
        mapReady = true
        pushPlacesToMap()
        vm.refreshParkingFromDb()
        val stations = vm.aqiStations.value.orEmpty()
        if (stations.isNotEmpty()) pushAqiToMap(stations)
    }

    // ── Build JSON for places ─────────────────────────────────────────────
    private fun pushPlacesToMap() {
        val all = AthensData.parks + AthensData.squares + AthensData.transit
        val arr = JSONArray()
        all.forEach { p ->
            arr.put(JSONObject().apply {
                put("id",          p.id)
                put("name",        p.name)
                put("type",        p.type.name)
                put("typeLabel",   p.type.label)
                put("emoji",       p.type.emoji)
                put("colorHex",    p.type.colorHex)
                put("lat",         p.lat)
                put("lng",         p.lng)
                put("description", p.description)
                put("lines",       JSONArray(p.lines))
            })
        }
        evalJs("addPlaces('${arr.toString().escapeForJs()}')")
    }
    private fun pushParkingToMap() {
        val dbData = vm.parkingData.value
        val arr = JSONArray()

        if (!dbData.isNullOrEmpty()) {
            dbData.forEach { p ->
                arr.put(JSONObject().apply {
                    put("id", p["id"])
                    put("name", p["name"])
                    put("lat", p["lat"])
                    put("lng", p["lng"])
                    put("total", p["total"])
                    put("available", p["available"])
                    put("price", p["price"])
                    put("address", p["address"])
                    put("occupancyLevel", p["occupancyLevel"])
                })
            }
        } else {
            AthensData.parking.forEach { p ->
                arr.put(JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("lat", p.lat)
                    put("lng", p.lng)
                    put("total", p.totalSpots)
                    put("available", p.availableSpots)
                    put("price", p.pricePerHour)
                    put("address", p.address)
                    put("occupancyLevel", p.occupancyLevel)
                })
            }
        }
        evalJs("addParking('${arr.toString().escapeForJs()}')")
    }
    private fun pushAqiToMap(stations: List<com.athens.lifeguide.data.models.AqiStation>) {
        val arr = JSONArray()
        stations.forEach { s ->
            arr.put(JSONObject().apply {
                put("uid",  s.uid)
                put("name", s.name)
                put("lat",  s.lat)
                put("lng",  s.lng)
                put("aqi",  s.aqi)
            })
        }
        evalJs("addAqiStations('${arr.toString().escapeForJs()}')")
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                evalJs("updateMyLocation(${loc.latitude},${loc.longitude})")
            }
        }
    }

    private fun checkAndFetchLocation() {
        val fine   = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), fine) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
            requestContinuousLocation()
        } else {
            locationLauncher.launch(arrayOf(fine, coarse))
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestContinuousLocation() {
        val req = LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 3000L).build()
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .requestLocationUpdates(req, locationCallback, android.os.Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    evalJs("updateMyLocation(${loc.latitude},${loc.longitude})")
                    evalJs("goToLocation(${loc.latitude},${loc.longitude})")
                } else {
                    Toast.makeText(requireContext(), "Δεν βρέθηκε τοποθεσία", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ── JS bridge ─────────────────────────────────────────────────────────
    inner class JsBridge {
        @JavascriptInterface
        fun onMapReady() = requireActivity().runOnUiThread { this@MapFragment.onMapReady() }

        @JavascriptInterface
        fun saveFav(id: String, name: String, type: String, lat: Double, lng: Double, desc: String) {
            requireActivity().runOnUiThread {
                vm.saveFavorite(id, name, type, lat, lng, desc)
            }
        }
        @JavascriptInterface
        fun reserveParking(id: String, name: String, price: Double) {
            requireActivity().runOnUiThread {
                // Show confirmation dialog
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Κράτηση Θέσης")
                    .setMessage("Θέλετε να κρατήσετε θέση στο «$name» για 15 λεπτά;\n\nΤιμή: €$price/ώρα")
                    .setPositiveButton("Κράτηση") { _, _ ->
                        vm.reserveParking(id, name, price)
                    }
                    .setNegativeButton("Ακύρωση", null)
                    .show()
            }
        }
    }

    private fun evalJs(js: String) {
        b.webMap.post { b.webMap.evaluateJavascript(js, null) }
    }

    private fun String.escapeForJs() =
        replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
