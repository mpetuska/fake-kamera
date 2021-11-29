package dev.petuska.fake.kamera.store

sealed interface AppAction {
  data class SetInputDevice(val device: String?) : AppAction
  data class SetOutputDevice(val device: String?) : AppAction
  data class SetDevices(val devices: Set<String>) : AppAction
}
