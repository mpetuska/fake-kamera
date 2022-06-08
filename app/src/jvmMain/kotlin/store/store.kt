package dev.petuska.fake.kamera.store

import androidx.compose.runtime.Composable
import dev.petuska.fake.kamera.util.selectState
import org.kodein.di.DI
import org.reduxkotlin.Store
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import org.reduxkotlin.createThunkMiddleware
import java.io.File

const val OutputDeviceName = "/dev/video40"

fun DI.loadStore(): Store<AppState> {
  val devices =
    File("/dev")
      .listFiles()
      ?.filter { it.name.startsWith("video") }
      ?.map(File::getAbsolutePath)
      ?.sorted()
      ?.toMutableList()
      ?: mutableListOf()
  val outputDevice = devices.firstOrNull { it == OutputDeviceName }
  return createStore(
    appReducer,
    AppState(devices = devices, inputDevicePath = devices.firstOrNull(), outputDevicePath = outputDevice),
    applyMiddleware(createThunkMiddleware())
  )
}

@Composable
inline fun <TSlice> selectState(crossinline selector: AppState.() -> TSlice) = selectState(selector)
