package dev.petuska.fake.kamera.view

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dev.petuska.fake.kamera.store.AppAction
import dev.petuska.fake.kamera.store.outputDeviceName
import dev.petuska.fake.kamera.store.selectState
import dev.petuska.fake.kamera.util.rememberDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FakeCameraCreator() {
  val outputDevice by selectState { outputDevice }
  val scope = rememberCoroutineScope()
  val dispatch = rememberDispatcher()
  Button(
      onClick = {
        scope.launch {
          withContext(Dispatchers.IO) {
            if (outputDevice == null) {
              Runtime.getRuntime()
                  .exec(
                      "sudo modprobe v4l2loopback devices=1 card_label=\"My Fake Webcam\" exclusive_caps=1 video_nr=40")
                  .onExit()
                  .await()
                  .let {
                    if (it.exitValue() != 0) {
                      error("Failed to create a fake camera")
                    } else {
                      dispatch(AppAction.SetOutputDevice(outputDeviceName))
                    }
                  }
            } else {
              Runtime.getRuntime()
                  .exec("sudo modprobe --remove v4l2loopback")
                  .onExit()
                  .await()
                  .let {
                    if (it.exitValue() != 0) {
                      error("Failed to remove a fake camera")
                    } else {
                      dispatch(AppAction.SetOutputDevice(null))
                    }
                  }
            }
          }
        }
      },
  ) { Text(if (outputDevice == null) "Create" else "Remove" + " fake camera") }
}
