package dev.petuska.fake.kamera.view

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.petuska.fake.kamera.service.ShellService
import dev.petuska.fake.kamera.store.AppAction
import dev.petuska.fake.kamera.store.outputDeviceName
import dev.petuska.fake.kamera.store.selectState
import dev.petuska.fake.kamera.util.VideoDevice
import dev.petuska.fake.kamera.util.rememberDispatcher
import dev.petuska.fake.kamera.util.rememberMutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Composable
fun FakeCameraCreator() {
  val outputDevice by selectState { outputDevice }
  val dispatch = rememberDispatcher()
  val defaultOutputDevice: VideoDevice by rememberInstance(null) { outputDeviceName }
  var password by rememberMutableStateOf<String?> { System.getenv("FK_PASSWORD") }
  var requestPassword by rememberMutableStateOf { false }
  var running by rememberMutableStateOf { false }
  LaunchedEffect(password, outputDevice, running) {
    val pwd = password
    if (running && pwd != null) {
      launch(Dispatchers.IO) {
        var device: VideoDevice? = outputDevice
        var error = false
        if (outputDevice == null) {
          ShellService()
            .executeSudo(
              "modprobe v4l2loopback devices=1 card_label=\"My Fake Webcam\" exclusive_caps=1 video_nr=40",
              pwd
            )
            .catch {
              error = true
              it.printStackTrace()
            }
            .collect { println(it) }
          device = defaultOutputDevice
        } else {
          ShellService()
            .executeSudo("modprobe --remove v4l2loopback", pwd)
            .catch {
              error = true
              it.printStackTrace()
            }
            .collect() { println(it) }
          device = null
        }
        if (!error) {
          dispatch(AppAction.SetOutputDevice(device))
        }
        running = false
      }
    } else if (running) {
      requestPassword = true
    }
  }
  PasswordDialog(
    requestPassword,
    onSubmit = {
      password = it
      requestPassword = false
    },
    onDismiss = {
      requestPassword = false
      running = false
    }
  )
  Button(
    enabled = !running,
    onClick = { running = true },
  ) { Text(if (outputDevice == null) "Create" else "Remove" + " fake camera") }
}
