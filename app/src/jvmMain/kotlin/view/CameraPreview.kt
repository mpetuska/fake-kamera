package dev.petuska.fake.kamera.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import dev.petuska.fake.kamera.effect.FrameEffect
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.toImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.opencv.core.Mat

@Composable
fun CameraPreview(
  device: VideoDeviceInput?,
  enabled: Boolean,
  fps: UInt? = null,
  frameEffect: FrameEffect = FrameEffect.NoOp,
) {
  val image by produceState<ImageBitmap?>(null, device, enabled) {
    if (enabled && device != null) {
      launch(Dispatchers.Default) { startStream(device, frameEffect, fps) }
    }
  }
  LaunchedEffect(enabled) {
    if (!enabled) {
      device?.close()
    }
  }
  Box(
    modifier =
    Modifier.border(BorderStroke(1.dp, Color.Gray))
      .width(640.dp)
      .height(480.dp)
      .padding(0.dp)
  ) {
    image?.let {
      Image(
        bitmap = it,
        contentDescription = "camera-preview",
//        alpha = if (enabled) 1.0f else 0.25f,
        alpha = 1.0f,
        modifier = Modifier.fillMaxSize().padding(0.dp)
      )
    }
  }
}

private suspend fun ProduceStateScope<ImageBitmap?>.startStream(
  device: VideoDeviceInput,
  effect: FrameEffect,
  fps: UInt? = null,
) {
  device.open(fps)
  device
    .stream()
    .filterNot(Mat::empty)
    .map(effect)
    .map(Mat::toImageBitmap)
    .collectLatest { value = it }
}
