package dev.petuska.fake.kamera

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.petuska.fake.kamera.store.loadStore
import dev.petuska.fake.kamera.util.invoke
import dev.petuska.fake.kamera.view.App
import nu.pattern.OpenCV

actual suspend fun main(vararg args: String) {
  OpenCV.loadShared()
  val store = loadStore()
  application {
    Window(title = "Fake Kamera", onCloseRequest = ::exitApplication) { store { App() } }
  }
}
