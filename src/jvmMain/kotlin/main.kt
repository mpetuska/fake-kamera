package dev.petuska.fake.kamera

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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.petuska.fake.kamera.jni.FakeCam
import dev.petuska.fake.kamera.util.rememberMutableStateOf
import dev.petuska.fake.kamera.view.CameraSelector
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nu.pattern.OpenCV
import org.opencv.core.MatOfByte
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio

@Preview
@Composable
fun App() {
  var image by remember { mutableStateOf<Painter?>(null) }
  val capture = remember {
    VideoCapture().also {
      it.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280.0)
      it.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720.0)
      it.set(Videoio.CAP_PROP_FPS, 60.0)
    }
  }
  val scope = rememberCoroutineScope()
  var device by rememberMutableStateOf<String?> { null }
  MaterialTheme {
    Column {
      CameraSelector(onChange = { device = it })
      Button(
          onClick = {
            image = null
            scope.launch { image = device?.let { capture.capture(it) } }
          }) { Text("Selfie!") }
      image?.let {
        Image(it, "screenshot", modifier = Modifier.wrapContentSize(Alignment.Center, true))
      }
    }
  }
}

private suspend fun VideoCapture.capture(device: String): Painter {
  open(device)
  lateinit var mat: MatOfByte

  val out =
      FakeCam(
          40,
          1280,
          720,
      )
  while (true) {
    mat = MatOfByte().also(::read)
    //        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB)
//    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2YUV_I420)
//    Imgproc.resize(mat, mat, Size(1280.0, 720.0))
//    Imgcodecs.imencode(".jpeg", mat, mat)

    if (true) {
      withContext(Dispatchers.IO) {
//        val bytes = mat.toArray()
//        out.writeFrame(bytes).also {
//          println("Writing Frame ${bytes.size} $it")
//        }
        out.writeMat(mat)
//        delay(500L)
      }
    } else {
      break
    }
  }
  release()
  return withContext(Dispatchers.IO) { ImageIO.read(ByteArrayInputStream(mat.toArray())) }
      .toPainter()
}

fun main() {
  OpenCV.loadShared()
  application { Window(title = "Fake Kamera", onCloseRequest = ::exitApplication) { App() } }
}
