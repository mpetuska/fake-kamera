package dev.petuska.fake.kamera.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Controls(
  streaming: Boolean,
  previewing: Boolean,
  previewingStream: Boolean,
  inputDevicePath: String?,
  outputDevicePath: String?,
  onStreaming: () -> Unit,
  onPreviewing: () -> Unit,
  onPreviewingStream: () -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    FakeCameraCreator()
    Button(
      onClick = onStreaming,
      colors =
      if (previewing) {
        ButtonDefaults.buttonColors(
          containerColor = Color.Red,
        )
      } else {
        ButtonDefaults.buttonColors()
      },
      enabled = inputDevicePath != null
    ) {
      Text(if (!previewing) "Preview Camera" else "Stop Camera Preview")
    }
    Button(
      onClick = onPreviewing,
      colors = if (streaming) {
        ButtonDefaults.buttonColors(
          containerColor = Color.Red,
        )
      } else {
        ButtonDefaults.buttonColors()
      },
      enabled = inputDevicePath != null && outputDevicePath != null
    ) {
      Text(if (!streaming) "Stream" else "Stop Stream")
    }
    Button(
      onClick = onPreviewingStream,
      enabled = streaming,
      colors =
      if (previewingStream && streaming) {
        ButtonDefaults.buttonColors(
          containerColor = Color.Red,
        )
      } else {
        ButtonDefaults.buttonColors()
      }
    ) { Text(if (!previewingStream) "Preview Stream" else "Stop Stream Preview") }
  }
}
