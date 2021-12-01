package dev.petuska.fake.kamera.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.petuska.fake.kamera.service.StreamService
import dev.petuska.fake.kamera.store.selectState
import dev.petuska.fake.kamera.util.rememberMutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Preview
@Composable
fun App() {
  MaterialTheme {
    val inputDevice by selectState { inputDevice }
    val outputDevice by selectState { outputDevice }
    val effects by selectState { effects }
    var previewing by rememberMutableStateOf { false }
    var streaming by rememberMutableStateOf { false }
    var previewingStream by rememberMutableStateOf { false }
    val fps by selectState { fps }
    LaunchedEffect(streaming, outputDevice, inputDevice, fps, effects) {
      if (streaming) {
        val id = inputDevice
        val od = outputDevice
        if (id != null && od != null) {
          val ss = StreamService(id, od, fps)
          launch(Dispatchers.IO) { ss.stream(effects) }.invokeOnCompletion { ss.close() }
        }
      }
    }
    MaterialTheme {
      Column(modifier = Modifier.fillMaxHeight()) {
        CameraSelector()
        FakeCameraCreator()
        Button(
            onClick = {
              streaming = false
              previewing = !previewing
            },
            colors =
                if (previewing) {
                  ButtonDefaults.buttonColors(
                      backgroundColor = Color.Red, contentColor = Color.Black)
                } else {
                  ButtonDefaults.buttonColors()
                },
            enabled = inputDevice != null) {
          Text(if (!previewing) "Preview Camera" else "Stop Camera Preview")
        }
        Button(
            onClick = {
              previewing = false
              streaming = !streaming
            },
            colors =
                if (streaming) {
                  ButtonDefaults.buttonColors(
                      backgroundColor = Color.Red, contentColor = Color.Black)
                } else {
                  ButtonDefaults.buttonColors()
                },
            enabled = inputDevice != null && outputDevice != null) {
          Text(if (!streaming) "Stream" else "Stop Stream")
        }
        Button(
            onClick = {
              previewing = false
              previewingStream = !previewingStream
            },
            enabled = streaming,
            colors =
                if (previewingStream && streaming) {
                  ButtonDefaults.buttonColors(
                      backgroundColor = Color.Red, contentColor = Color.Black)
                } else {
                  ButtonDefaults.buttonColors()
                }) { Text(if (!previewingStream) "Preview Stream" else "Stop Stream Preview") }
        Row {
          CameraPreview(inputDevice, previewing, fps)
          CameraPreview(outputDevice?.toInput(), streaming && previewingStream, fps)
        }
      }
    }
  }
}
