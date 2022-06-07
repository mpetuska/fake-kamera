package dev.petuska.fake.kamera.store

import dev.petuska.fake.kamera.util.VideoDeviceOutput
import org.reduxkotlin.reducerForActionType

val appReducer =
  reducerForActionType<AppState, AppAction> { state, action ->
    when (action) {
      is AppAction.SetDevices -> state.copy(devices = action.devices.sortedBy { it.path })
      is AppAction.SetInputDevice ->
        state.copy(
          inputDevice =
          action.device?.let { device ->
            state.devices.firstOrNull { it.path == device.path }
          }
        )
      is AppAction.SetOutputDevice ->
        state.copy(
          outputDevice =
          action.device?.let { if (it is VideoDeviceOutput) it else it.toOutput() }
        )
    }
  }
