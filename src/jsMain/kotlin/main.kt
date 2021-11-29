package dev.petuska.fake.kamera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlin.js.json
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Video
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.mediacapture.MediaDeviceInfo
import org.w3c.dom.mediacapture.MediaDeviceKind
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.dom.mediacapture.VIDEOINPUT

fun main() {
  renderComposable(rootElementId = "root") {
    Style(AppStyle)
    Div(attrs = { classes(AppStyle.cotainer) }) {
      var videoDevices by remember { mutableStateOf(listOf<MediaDeviceInfo>()) }
      val scope = rememberCoroutineScope()
      var camera by remember { mutableStateOf<MediaDeviceInfo?>(null) }
      LaunchedEffect(null) { loadVideoDevices { videoDevices = it } }
      H1 { Text("Fake Kamera Test") }
      CameraBox(camera)
      Div {
        CameraSelector(camera, videoDevices, { camera = it }) { it.label }
        Button(
            attrs = {
              onClick {
                scope.launch { loadVideoDevices { videoDevices = it } }
                camera = null
              }
            }) { Text("Refresh") }
      }
    }
  }
}

private suspend fun loadVideoDevices(onLoaded: (List<MediaDeviceInfo>) -> Unit) {
  val devices =
      window.navigator.mediaDevices.enumerateDevices().await().filter {
        it.kind == MediaDeviceKind.VIDEOINPUT
      }
  console.info("VideoDevices", devices.toTypedArray())
  onLoaded(devices)
}

object AppStyle : StyleSheet() {
  val cotainer by style {
    position(Position.Relative)
    width(100.percent)
    height(100.percent)
    display(DisplayStyle.Flex)
    flexDirection(FlexDirection.Column)
    alignItems(AlignItems.Center)
    "> *" { marginBottom(1.em) }
  }
  val cameraBox by style {
    backgroundColor(Color.lightgray)
    border(1.px, LineStyle.Solid, Color.black)
    minWidth(640.px)
    minHeight(480.px)
    borderRadius(25.px)
    boxSizing("border-box")
  }
  val cameraPlaceholder by style {
    opacity(30.percent)
    padding(1.em)
    display(DisplayStyle.Flex)
    alignItems(AlignItems.Center)
    justifyContent(JustifyContent.Center)
  }
}

@Composable
private fun CameraBox(camera: MediaDeviceInfo?) {
  var src by remember { mutableStateOf<MediaStream?>(null) }
  fun setSrc(new: MediaStream?) {
    src?.getTracks()?.forEach { track ->
      console.info("StoppingTrack", track)
      track.stop()
    }
    src = new
  }
  LaunchedEffect(camera) {
    if (camera != null) {
      window.navigator.getUserMedia(
          MediaStreamConstraints(video = json("deviceId" to json("exact" to camera.deviceId))),
          { setSrc(it) },
          { console.error("Cannot open stream for media device $camera; $it") })
    } else {
      setSrc(null)
    }
  }
  if (src != null) {
    Video(
        attrs = {
          classes(AppStyle.cameraBox)
          attr("autoplay", "true")
        }) {
      DisposableRefEffect { onDispose { setSrc(null) } }
      DomSideEffect(src) {
        it.srcObject = src
        console.info("Playing", src)
        if (src != null) {
          it.play()
        } else {
          it.pause()
        }
      }
    }
  } else {
    Div(attrs = { classes(AppStyle.cameraBox, AppStyle.cameraPlaceholder) }) {
      Text("No camera selected")
    }
  }
}

@Composable
private fun <T> CameraSelector(
    selected: T?,
    devices: List<T>,
    onChange: (T?) -> Unit,
    label: (T) -> String
) {
  val dMap = remember(devices) { devices.associateBy(label) }
  Select(
      attrs = {
        onChange {
          val vDev = dMap[it.value]
          console.info("SelectedVideoDevice", vDev)
          onChange(vDev)
        }
      }) {
    if (selected == null) {
      Option(
          "",
          attrs = {
            selected()
            hidden()
          }) { Text("Select a camera...") }
    }
    devices.forEach {
      val l = label(it)
      Option(l, attrs = { if (selected == it) selected() }) { Text(l) }
    }
  }
}
