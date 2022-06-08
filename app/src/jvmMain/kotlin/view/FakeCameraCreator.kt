package dev.petuska.fake.kamera.view

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.petuska.fake.kamera.service.ShellService
import dev.petuska.fake.kamera.store.AppAction
import dev.petuska.fake.kamera.store.OutputDeviceName
import dev.petuska.fake.kamera.store.selectState
import dev.petuska.fake.kamera.util.logger
import dev.petuska.fake.kamera.util.rememberDispatcher
import dev.petuska.fake.kamera.util.rememberMutableStateOf
import kotlinx.coroutines.supervisorScope

@Composable
fun FakeCameraCreator() {
  val logger = logger("FakeCameraCreator")
  val outputDevicePath by selectState { outputDevicePath }
  val dispatch = rememberDispatcher()
  var password by rememberMutableStateOf<String?> { System.getenv("FK_PASSWORD") }
  var requestPassword by rememberMutableStateOf { false }
  var running by rememberMutableStateOf { false }
  LaunchedEffect(password, outputDevicePath, running) {
    val pwd = password
    if (running && pwd != null) {
      supervisorScope {
        var device: String? = outputDevicePath
        var isError = false
        if (device == null) {
          ShellService
            .executeSudo(
              "modprobe v4l2loopback devices=1 card_label=\"My Fake Webcam\" exclusive_caps=1 video_nr=40",
              pwd
            )
            .collect { (error, msg) ->
              if (error) {
                isError = true
                logger.error { msg }
              } else {
                logger.debug { msg }
              }
            }
          device = OutputDeviceName
        } else {
          ShellService
            .executeSudo("modprobe --remove v4l2loopback", pwd)
            .collect { (error, msg) ->
              if (error) {
                isError = true
                logger.error { msg }
              } else {
                logger.debug { msg }
              }
            }
          device = null
        }
        if (!isError) {
          if (device == null) {
            logger.info { "Removed fake camera $outputDevicePath" }
          } else {
            logger.info { "Created fake camera $device" }
          }
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
  ) { Text(if (outputDevicePath == null) "Create" else "Remove" + " fake camera") }
}
