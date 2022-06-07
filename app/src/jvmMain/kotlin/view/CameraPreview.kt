package dev.petuska.fake.kamera.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import dev.petuska.fake.kamera.effect.FrameEffect
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.toImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.opencv.core.Mat

@Composable
fun CameraPreview(
  device: VideoDeviceInput?,
  enabled: Boolean,
  fps: UInt? = null,
  processFrame: FrameEffect = FrameEffect.NoOp,
) {
  val image by
  produceState<ImageBitmap?>(null, device, enabled) {
    if (enabled && device != null) {
      launch(Dispatchers.Default) { startStream(device, processFrame, fps) }
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
      .width(320.dp)
      .height(240.dp)
      .padding(0.dp)
  ) {
    image?.let {
      Image(
        bitmap = it,
        contentDescription = "camera-preview",
        alpha = if (enabled) 1.0f else 0.25f,
        modifier = Modifier.fillMaxSize().padding(0.dp)
      )
    }
  }
}

private suspend fun ProduceStateScope<ImageBitmap?>.startStream(
  device: VideoDeviceInput,
  processFrame: FrameEffect,
  fps: UInt? = null,
) {
  device.open(fps)
  device
    .stream()
    .filterNot(Mat::empty)
    .conflate()
    .map { processFrame(it) }
    .map(Mat::toImageBitmap)
    .collect { value = it }
}
