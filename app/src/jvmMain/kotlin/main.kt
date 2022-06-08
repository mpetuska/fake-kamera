package dev.petuska.fake.kamera

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.petuska.fake.kamera.store.loadStore
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.VideoDeviceOutput
import dev.petuska.fake.kamera.util.WithLogFactory
import dev.petuska.fake.kamera.util.invoke
import dev.petuska.fake.kamera.view.App
import nu.pattern.OpenCV
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindings.UnboundedScope
import org.kodein.di.compose.withDI
import org.kodein.di.factory
import org.kodein.di.scoped

object AppScope : UnboundedScope()

fun main(vararg args: String) {
  println(args.toList())
  OpenCV.loadLocally()
  val di = DI {
    bind {
      scoped(AppScope).factory { data: WithLogFactory<String> ->
        VideoDeviceInput(
          factory = data.factory,
          path = data.data,
          fps = 60,
          width = 640,
          height = 480,
          channels = 3,
        )
      }
    }
    bind {
      scoped(AppScope).factory { data: WithLogFactory<String> ->
        VideoDeviceOutput(
          factory = data.factory,
          path = data.data,
          fps = 60,
          width = 640,
          height = 480,
          channels = 2,
        )
      }
    }
  }
  val store = di.loadStore()

  application {
    Window(
      title = "Fake Kamera",
      onCloseRequest = ::exitApplication,
      state = WindowState(placement = WindowPlacement.Maximized)
    ) {
      withDI(di) { store { App() } }
    }
  }
}
