package dev.petuska.fake.kamera.store

import dev.petuska.fake.kamera.util.VideoDevice
import dev.petuska.fake.kamera.util.VideoDeviceInput

sealed interface AppAction {
  data class SetInputDevice(val device: VideoDevice?) : AppAction
  data class SetOutputDevice(val device: VideoDevice?) : AppAction
  data class SetDevices(val devices: Set<VideoDeviceInput>) : AppAction
}
