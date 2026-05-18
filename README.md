# Athens Life Guide 🏛️

Athens Life Guide is a feature-rich Android application designed to help users navigate and explore the city of Athens. It provides a comprehensive, interactive map experience that integrates points of interest, real-time air quality data, and an interactive parking reservation system.

## 🌟 Key Features

* **Interactive Hybrid Map**: Built using a highly optimized WebView and Leaflet.js, avoiding heavy native MAP SDK lock-ins while retaining full customizability.
* **Smart Filtering**: Easily toggle map markers for Parks 🌳, Squares ⛲, Transit stations (MMM) 🚇, Parking 🅿️, and live traffic lights 🚦.
* **Persistent GPS Tracking**: Continuously tracks and centers on your real-time location using `FusedLocationProviderClient`.
* **Dynamic Compass Rotation**: Includes a hardware-accelerated gyroscope lock. When enabled, the map rotates seamlessly around your fixed GPS position based on the direction your phone points (similar to Google Maps navigation mode).
* **Parking Reservations**: Browse available parking spots, check occupancy and pricing limits, and instantly reserve a parking space for 15 minutes. Includes a sleek UI to view 'Active' and 'Cancelled' reservations.
* **Environmental Awareness**: Real-time AQI (Air Quality Index) station markers visually indicate the air quality across the city using intuitive color-coding.

## 🛠️ Tech Stack

* **Language**: Kotlin
* **Architecture**: MVVM (Model-View-ViewModel) with LiveData
* **Mapping**: Leaflet.js + Geoapify Tiles (Loaded locally via Android WebView / HTML / JS)
* **Location & Sensors**: Google Play Services Location API (`Priority.PRIORITY_HIGH_ACCURACY`), Android `SensorManager` (Rotation Vector)
* **UI**: Android XML Layouts, Material Design Components

## 🏗️ Architecture Note: Hybrid Mapping

To maximize flexibility and reduce SDK overhead, the map component (`MapFragment`) uses a `WebView` parsing a local `map.html` file from the `res/raw` directory. 
Communication between the native Android Kotlin code and the JavaScript map engine is handled seamlessly via an embedded `@JavascriptInterface` bridge (`evalJs()`). This allows Android to pass GPS coordinates, gyroscope rotation degrees, and complex JSON arrays to JS, while HTML buttons (like the `⭐ Favorite` and `🅿️ Reserve` buttons) instantly trigger native Android ViewModel functions.

## 🚀 Getting Started

1. Clone this repository directly into Android Studio.
2. Wait for Gradle to sync dependencies.
3. (Optional) Provide a valid Geoapify API key in `map.html` if the current tile limitations are exceeded.
4. Run the project on an emulator (with location mocking enabled) or a physical Android device.

*Note: For the best experience, run on a physical device to properly test the Sensor/Gyroscope compass rotation features!*