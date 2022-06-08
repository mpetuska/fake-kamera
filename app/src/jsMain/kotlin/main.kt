package dev.petuska.fake.kamera

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.mediacapture.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.json

suspend fun main(vararg args: String) {
  renderComposable(rootElementId = "root") {
    Style(AppStyle)
    Div(attrs = { classes(AppStyle.cotainer) }) {
      var videoDevices by remember { mutableStateOf(listOf<MediaDeviceInfo>()) }
      val scope = rememberCoroutineScope()
      var camera by remember { mutableStateOf<MediaDeviceInfo?>(null) }
      LaunchedEffect(Unit) { loadVideoDevices { videoDevices = it } }
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
          }
        ) { Text("Refresh") }
      }
    }
  }
}

@Suppress("UnusedPrivateMember")
private suspend fun promptMediaAccess() = suspendCoroutine<Unit> { cont ->
  window.navigator.getUserMedia(
    MediaStreamConstraints(video = json()),
    {
      cont.resume(Unit)
    },
    {
      console.error("Cannot access media devices", it)
      cont.resume(Unit)
    }
  )
}

private suspend fun loadVideoDevices(onLoaded: (List<MediaDeviceInfo>) -> Unit) {
//  promptMediaAccess()
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
        { console.error("Cannot open stream for media device $camera; $it") }
      )
    } else {
      setSrc(null)
    }
  }
  if (src != null) {
    Video(
      attrs = {
        classes(AppStyle.cameraBox)
        attr("autoplay", "true")
      }
    ) {
      DisposableEffect(Unit) { onDispose { setSrc(null) } }
      DisposableEffect(src) {
        scopeElement.srcObject = src
        console.info("Playing", src)
        if (src != null) {
          scopeElement.play()
        } else {
          scopeElement.pause()
        }
        onDispose { }
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
    }
  ) {
    if (selected == null) {
      Option(
        "",
        attrs = {
          selected()
          hidden()
        }
      ) { Text("Select a camera...") }
    }
    devices.forEachIndexed { i, it ->
      val l = label(it)
      Option(l, attrs = { if (selected == it) selected() }) { Text(l.takeIf(String::isNotBlank) ?: "$i") }
    }
  }
}
