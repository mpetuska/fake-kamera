package dev.petuska.fake.kamera.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import dev.petuska.fake.kamera.jni.FakeCam
import dev.petuska.fake.kamera.store.selectState
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.MatOfByte
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio

@Preview
@Composable
fun App() {
  MaterialTheme {
    var image by remember { mutableStateOf<BufferedImage?>(null) }
    val capture = remember {
      VideoCapture().also {
        it.set(Videoio.CAP_PROP_FRAME_WIDTH, 640.0)
        it.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480.0)
        it.set(Videoio.CAP_PROP_FPS, 60.0)
        it.set(Videoio.CAP_PROP_CONVERT_RGB, 1.0)
      }
    }
    val scope = rememberCoroutineScope()
    val inputDevice by selectState { inputDevice }
    val outputDevice by selectState { outputDevice }
    MaterialTheme {
      Column {
        CameraSelector()
        FakeCameraCreator()
        Button(
            onClick = {
              image = null
              scope.launch { image = inputDevice?.let { capture(it) } }
            }) { Text("Selfie!") }
        Button(
            onClick = {
              image = null
              scope.launch { stream(inputDevice!!, outputDevice!!) }
            }) { Text("Start") }
        image?.let {
          Image(
              it.toComposeImageBitmap(),
              "screenshot",
              modifier = Modifier.wrapContentSize(Alignment.Center, true))
        }
      }
    }
  }
}

private fun CoroutineScope.stream(inputDevice: String, outputDevice: String): Job {
  val input = VideoCapture(inputDevice)
  val output =
      FakeCam(
          outputDevice,
          MatOfByte().also(input::read),
      )
  return launch(Dispatchers.Default) {
    while (isActive) {
      val mat = MatOfByte().also(input::read)
      output.writeFrame(mat)
    }
  }
      .apply {
        invokeOnCompletion {
          input.release()
          output.close()
        }
      }
}

private suspend fun capture(inputDevice: String): BufferedImage {
  val mat =
      withContext(Dispatchers.IO) {
        val input = VideoCapture(inputDevice)
        val mat = MatOfByte().also(input::read)
        input.release()
        mat
      }
  return with(mat) {
    val image = BufferedImage(width(), height(), BufferedImage.TYPE_3BYTE_BGR)
    val data = (image.raster.dataBuffer as DataBufferByte).data
    get(0, 0, data)
    image
  }
}
