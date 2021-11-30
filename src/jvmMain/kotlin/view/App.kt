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
import dev.petuska.fake.kamera.util.rememberMutableStateOf
import dev.petuska.fake.kamera.util.toBufferedImage
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Mat
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
    var streamJob by rememberMutableStateOf<Job?> { null }
    var previewJob by rememberMutableStateOf<Job?> { null }
    MaterialTheme {
      Column {
        CameraSelector()
        FakeCameraCreator()
        Button(
            onClick = {
              scope.launch { image = inputDevice?.let { capture(it) } }
              image = null
            },
            enabled = previewJob == null && streamJob == null) { Text("Selfie!") }
        Button(
            onClick = {
              streamJob?.cancel()?.also { streamJob = null }
                  ?: run { streamJob = scope.launch { stream(inputDevice!!, outputDevice!!) } }
              image = null
            },
            enabled = previewJob == null) {
          Text(if (streamJob == null) "Stream" else "Stop Stream")
        }
        Button(
            onClick = {
              previewJob?.cancel()?.also { previewJob = null }
                  ?: run {
                    previewJob =
                        scope.launch {
                          stream(inputDevice!!, outputDevice!!) { image = it.toBufferedImage() }
                        }
                  }
              image = null
            },
            enabled = streamJob == null) {
          Text(if (previewJob == null) "Preview" else "Stop Preview")
        }
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

private fun CoroutineScope.stream(
    inputDevice: String,
    outputDevice: String,
    onFrame: ((Mat) -> Unit)? = null
): Job {
  val fps = 60
  val input = getVideoCapture(inputDevice)
  val output =
      FakeCam(
          outputDevice,
          MatOfByte().also(input::read),
      )
  return launch(Dispatchers.Default) {
    while (isActive) {
      delay((1000 / fps).toLong())
      val mat = MatOfByte().also(input::read)
      onFrame?.invoke(mat) ?: output.writeFrame(mat)
    }
  }
      .apply {
        invokeOnCompletion {
          input.release()
          output.close()
        }
      }
}

fun getVideoCapture(device: String) =
    VideoCapture(device).apply {
      set(Videoio.CAP_PROP_FRAME_WIDTH, 720.0)
      set(Videoio.CAP_PROP_FRAME_HEIGHT, 1280.0)
      set(Videoio.CAP_PROP_FPS, 60.0)
      set(Videoio.CAP_PROP_CHANNEL, 3.0)
    }

private suspend fun capture(inputDevice: String): BufferedImage {
  val mat =
      withContext(Dispatchers.IO) {
        val input = getVideoCapture(inputDevice)
        val mat = MatOfByte().also(input::read)
        println("${mat.size()}")
        mat.also { input.release() }
      }
  return with(mat) {
    val image = BufferedImage(width(), height(), BufferedImage.TYPE_3BYTE_BGR)
    val data = (image.raster.dataBuffer as DataBufferByte).data
    get(0, 0, data)
    image
  }
}
