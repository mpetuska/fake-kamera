package dev.petuska.fake.kamera.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> ExposedDropdownMenu(
  items: List<T>,
  selected: T,
  onSelectionChange: (T) -> Unit,
  itemLabelBuilder: (T) -> String = { it.toString() },
  modifier: Modifier = Modifier,
  label: @Composable (() -> Unit)? = null
) {
  var expanded by remember { mutableStateOf(false) }
  var expandedFromInput by remember { mutableStateOf(false) }
  Box(
    modifier = modifier
      .wrapContentWidth(Alignment.Start)
      .padding(0.dp)
  ) {
    OutlinedTextField(
      value = itemLabelBuilder(selected),
      label = label,
      onValueChange = {},
      readOnly = true,
      modifier = modifier.fillMaxWidth()
        .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)), true)
        .onPointerEvent(PointerEventType.Release) {
          expandedFromInput = !expandedFromInput
          expanded = !expanded && expandedFromInput
        },
      singleLine = true,
      trailingIcon = {
        val rotation by animateFloatAsState(if (expanded) 180f else 0f)
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "select",
          modifier = Modifier.rotate(rotation).clip(CircleShape)
        )
      }
    )
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.fillMaxWidth()
    ) {
      items.forEach { item: T ->
        DropdownMenuItem(
          onClick = {
            onSelectionChange(item)
            expanded = false
            expandedFromInput = false
          }
        ) { Text(text = itemLabelBuilder(item)) }
      }
    }
  }
}
