package dev.petuska.fake.kamera.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.petuska.fake.kamera.component.ExposedDropdownMenu
import dev.petuska.fake.kamera.util.rememberMutableStateOf
import java.io.File

@Preview
@Composable
fun CameraSelector(onChange: (String) -> Unit) {
  val items = remember {
    File("/dev")
        .listFiles()
        ?.filter { it.name.startsWith("video") }
        ?.map { it.absolutePath }
        ?.sorted()
  }
  items?.let { it ->
    var selected by rememberMutableStateOf<String> { items[0].also(onChange) }
    ExposedDropdownMenu(
        items = it,
        selected = selected,
        onSelectionChange = { selection ->
          selected = selection
          onChange(selection)
        },
        label = { Text("Select Camera Device") })
  }
}
