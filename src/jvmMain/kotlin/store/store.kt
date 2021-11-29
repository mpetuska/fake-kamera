package dev.petuska.fake.kamera.store

import androidx.compose.runtime.Composable
import dev.petuska.fake.kamera.util.select
import java.io.File
import org.reduxkotlin.Store
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import org.reduxkotlin.createThunkMiddleware

const val outputDeviceName = "/dev/video40"

fun loadStore(): Store<AppState> {
  val devices =
      File("/dev")
          .listFiles()
          ?.filter { it.name.startsWith("video") }
          ?.map { it.absolutePath }
          ?.sorted()
          ?: listOf()
  val outputDevice = File(outputDeviceName).takeIf(File::exists)?.absolutePath
  return createStore(
      appReducer,
      AppState(devices = devices, inputDevice = devices.firstOrNull(), outputDevice = outputDevice),
      applyMiddleware(createThunkMiddleware()))
}

@Composable
inline fun <TSlice> selectState(crossinline selector: AppState.() -> TSlice) = select(selector)
