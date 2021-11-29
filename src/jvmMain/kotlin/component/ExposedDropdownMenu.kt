package dev.petuska.fake.kamera.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import java.awt.Cursor

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
  Box(modifier = modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart)) {
    OutlinedTextField(
        value = itemLabelBuilder(selected),
        label = label,
        onValueChange = {},
        readOnly = true,
        modifier =
            modifier.fillMaxWidth().pointerHoverIcon(PointerIcon(Cursor.getDefaultCursor()), true),
        singleLine = true,
        trailingIcon = {
          val rotation by animateFloatAsState(if (expanded) 180f else 0f)
          Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = "select",
              modifier =
                  Modifier.rotate(rotation)
                      .clip(CircleShape)
                      .clickable(role = Role.Button, onClick = { expanded = true }))
        })
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth()) {
      items.forEach { item: T ->
        DropdownMenuItem(
            onClick = {
              onSelectionChange(item)
              expanded = false
            }) { Text(text = itemLabelBuilder(item)) }
      }
    }
  }
}
