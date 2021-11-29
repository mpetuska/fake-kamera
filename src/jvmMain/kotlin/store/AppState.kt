package dev.petuska.fake.kamera.store

data class AppState(
    val devices: List<String>,
    val inputDevice: String? = null,
    val outputDevice: String? = null,
)
