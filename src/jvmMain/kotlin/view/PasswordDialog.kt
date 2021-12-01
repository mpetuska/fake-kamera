package dev.petuska.fake.kamera.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.petuska.fake.kamera.util.rememberMutableStateOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PasswordDialog(show: Boolean, onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
  val user = remember { System.getProperty("user.name") }
  var password by rememberMutableStateOf { "" }
  if (show) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Enter a password for $user") },
        text = { PasswordInputField(password) { password = it } },
        confirmButton = { Button(onClick = { onSubmit(password) }) { Text("Confirm") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } })
  }
}

@Composable
private fun PasswordInputField(value: String, onChange: (String) -> Unit) {
  var passwordVisibility: Boolean by remember { mutableStateOf(false) }
  OutlinedTextField(
      value = value,
      onValueChange = onChange,
      modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
      label = { Text("Password") },
      placeholder = { Text(text = "Password") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      colors =
          TextFieldDefaults.outlinedTextFieldColors(
              //              focusedBorderColor = Color.Purple500,
              unfocusedBorderColor = Color.Black,
              placeholderColor = Color.Gray,
              textColor = Color.Black),
      singleLine = true,
      isError = true,
      visualTransformation =
          if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
      trailingIcon = {
        val image = if (passwordVisibility) Icons.Filled.Menu else Icons.Outlined.Menu
        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
          Icon(imageVector = image, "")
        }
      })
}
