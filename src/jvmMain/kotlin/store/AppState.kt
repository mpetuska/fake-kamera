package dev.petuska.fake.kamera.store

import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.VideoDeviceOutput

data class AppState(
    val devices: List<VideoDeviceInput>,
    val inputDevice: VideoDeviceInput? = null,
    val outputDevice: VideoDeviceOutput? = null,
    val fps: UInt = 30u,
)
