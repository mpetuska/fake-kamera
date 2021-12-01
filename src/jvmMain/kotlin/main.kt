package dev.petuska.fake.kamera

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.petuska.fake.kamera.store.loadStore
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.invoke
import dev.petuska.fake.kamera.view.App
import nu.pattern.OpenCV
import org.kodein.di.DI
import org.kodein.di.bindMultiton
import org.kodein.di.compose.withDI

actual suspend fun main(vararg args: String) {
  OpenCV.loadShared()
  val di = DI {
    bindMultiton { device: String ->
      VideoDeviceInput(path = device, width = 1280, height = 720, channels = 3)
    }
  }
  val store = di.loadStore()

  application {
    Window(title = "Fake Kamera", onCloseRequest = ::exitApplication) {
      withDI(di) { store { App() } }
    }
  }
}
