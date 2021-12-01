package dev.petuska.fake.kamera.store

import androidx.compose.runtime.Composable
import dev.petuska.fake.kamera.util.VideoDevice
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.selectState
import java.io.File
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.reduxkotlin.Store
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import org.reduxkotlin.createThunkMiddleware

const val outputDeviceName = "/dev/video40"

fun DI.loadStore(): Store<AppState> {
  val devices =
      File("/dev")
          .listFiles()
          ?.filter { it.name.startsWith("video") }
          ?.map { di.direct.instance<String, VideoDeviceInput>(null, it.absolutePath) }
          ?.sortedBy(VideoDevice::path)
          ?.toMutableList()
          ?: mutableListOf()
  val outputDevice =
      devices.firstOrNull { it.path == outputDeviceName }?.let {
//        devices.remove(it)
        it.toOutput()
      }
  return createStore(
      appReducer,
      AppState(devices = devices, inputDevice = devices.firstOrNull(), outputDevice = outputDevice),
      applyMiddleware(createThunkMiddleware()))
}

@Composable
inline fun <TSlice> selectState(crossinline selector: AppState.() -> TSlice) = selectState(selector)
