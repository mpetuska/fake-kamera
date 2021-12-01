package dev.petuska.fake.kamera.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.petuska.fake.kamera.effect.HologramEffect
import dev.petuska.fake.kamera.store.selectState
import dev.petuska.fake.kamera.util.rememberMutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.opencv.core.Mat

@Preview
@Composable
fun App() {
  MaterialTheme {
    val inputDevice by selectState { inputDevice }
    val outputDevice by selectState { outputDevice }
    var previewing by rememberMutableStateOf { false }
    var streaming by rememberMutableStateOf { false }
    val fps by selectState { fps }
    LaunchedEffect(streaming, outputDevice, inputDevice) {
      if (streaming) {
        println("Starting Stream")
        outputDevice?.let { od ->
          inputDevice?.let { id ->
            launch(Dispatchers.IO) {
              id.stream().postProcess().collect {
                od.write(it)
//                delay(1000L / fps.toInt())
              }
            }
                .invokeOnCompletion {
                  od.close()
                  id.close()
                }
          }
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
            enabled = inputDevice != null) { Text(if (!previewing) "Preview" else "Stop Preview") }
        Button(
            onClick = {
              previewing = false
              streaming = !streaming
            },
            enabled = inputDevice != null && outputDevice != null) {
          Text(if (!streaming) "Stream" else "Stop Stream")
        }
        Row {
          CameraPreview(inputDevice, previewing)
          CameraPreview(outputDevice?.toInput(), streaming)
        }
      }
    }
  }
}

fun Flow<Mat>.postProcess(): Flow<Mat> {
  val effects = listOf(HologramEffect)
  return map { effects.fold(it.clone()) { frame, effect -> effect(frame) } }
}

// private fun CoroutineScope.stream(
//    inputDevice: String,
//    outputDevice: String,
//    onFrame: ((Mat) -> Unit)? = null
// ): Job {
//  val fps = 60
//  val input = getVideoCapture(inputDevice)
//  val output =
//      FakeCam(
//          outputDevice,
//          MatOfByte().also(input::read),
//      )
//  return launch(Dispatchers.Default) {
//    while (isActive) {
//      delay((1000 / fps).toLong())
//      val mat = MatOfByte().also(input::read).run { postProcess() }
//      onFrame?.invoke(mat) ?: output.writeFrame(mat)
//    }
//  }
//      .apply {
//        invokeOnCompletion {
//          input.release()
//          output.close()
//        }
//      }
// }

// private suspend fun capture(inputDevice: String): BufferedImage {
//  val mat =
//      withContext(Dispatchers.IO) {
//        val input = getVideoCapture(inputDevice)
//        val mat = MatOfByte().also(input::read)
//        println("${mat.size()}")
//        mat.also { input.release() }
//      }
//  return with(mat) {
//    val image = BufferedImage(width(), height(), BufferedImage.TYPE_3BYTE_BGR)
//    val data = (image.raster.dataBuffer as DataBufferByte).data
//    get(0, 0, data)
//    image
//  }
// }
