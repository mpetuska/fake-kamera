package dev.petuska.fake.kamera.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import dev.petuska.fake.kamera.component.ExposedDropdownMenu
import dev.petuska.fake.kamera.store.AppAction
import dev.petuska.fake.kamera.store.selectState
import dev.petuska.fake.kamera.util.rememberDispatcher

@Composable
fun CameraSelector() {
  val items by selectState { devices }
  val selected by selectState { inputDevicePath }
  val dispatch = rememberDispatcher()
  ExposedDropdownMenu(
    items = items,
    selected = selected,
    onSelectionChange = { dispatch(AppAction.SetInputDevice(it)) },
    itemLabelBuilder = { it.toString() },
    label = { Text("Select Input Device") }
  )
}
