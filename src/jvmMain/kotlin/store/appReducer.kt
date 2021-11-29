package dev.petuska.fake.kamera.store

import org.reduxkotlin.reducerForActionType

val appReducer =
    reducerForActionType<AppState, AppAction> { state, action ->
      when (action) {
        is AppAction.SetDevices -> state.copy(devices = action.devices.sorted())
        is AppAction.SetInputDevice -> state.copy(inputDevice = action.device)
        is AppAction.SetOutputDevice -> state.copy(outputDevice = action.device)
      }
    }
