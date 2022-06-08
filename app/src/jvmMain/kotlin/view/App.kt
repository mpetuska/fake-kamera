package dev.petuska.fake.kamera.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.petuska.fake.kamera.service.StreamService
import dev.petuska.fake.kamera.store.selectState
import dev.petuska.fake.kamera.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance

@Preview
@Composable
fun App() = LogProvider {
  val logger = logger("App")
  val factory = LoggerFactoryLocal.current
  val inputDevicePath by selectState { inputDevicePath }
  val outputDevicePath by selectState { outputDevicePath }
  val di = localDI().direct
  var inputDevice: VideoDeviceInput? by rememberMutableStateOf { null }
  var outputDevice: VideoDeviceOutput? by rememberMutableStateOf { null }
  DisposableEffect(inputDevicePath) {
    inputDevice = inputDevicePath?.let { di.instance(null, factory.data(it)) }
    onDispose { inputDevice?.close() }
  }
  DisposableEffect(outputDevicePath) {
    outputDevice = outputDevicePath?.let { di.instance(null, factory.data(it)) }
    onDispose { outputDevice?.close() }
  }
  val effect by selectState { effect }
  var previewing by rememberMutableStateOf { false }
  var streaming by rememberMutableStateOf { false }
  var previewingStream by rememberMutableStateOf { false }
  val fps by selectState { fps }
  LaunchedEffect(streaming, outputDevice, inputDevice, fps, effect) {
    if (streaming) {
      val id = inputDevice
      val od = outputDevice
      if (id != null && od != null) {
        logger.info { "Launching Stream: $id -> $od" }
        val ss = StreamService(id, od, fps)
        launch(Dispatchers.IO) { ss.stream(effect) }.invokeOnCompletion { ss.close() }
      }
    }
  }
  MaterialTheme {
    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
      CameraSelector()
      Controls(
        streaming = streaming,
        previewing = previewing,
        previewingStream = previewingStream,
        inputDevicePath = inputDevicePath,
        outputDevicePath = outputDevicePath,
        onStreaming = {
          streaming = false
          previewing = !previewing
        },
        onPreviewing = {
          previewing = false
          streaming = !streaming
          previewingStream = true
        },
        onPreviewingStream = {
          previewing = false
          previewingStream = !previewingStream
        }
      )
      Row(modifier = Modifier.wrapContentSize().align(Alignment.CenterHorizontally)) {
        CameraPreview(inputDevice, previewing, fps)
        CameraPreview(outputDevice?.toInput(), streaming && previewingStream, fps)
      }
      Logs()
    }
  }
}
