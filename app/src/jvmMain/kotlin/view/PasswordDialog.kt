package dev.petuska.fake.kamera.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.petuska.fake.kamera.util.rememberMutableStateOf

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PasswordDialog(show: Boolean, onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
  val user = remember { System.getProperty("user.name") }
  var password by rememberMutableStateOf { "" }
  val submit =
    remember(password) {
      fun(it: KeyEvent): Boolean {
        return when (it.key) {
          Key.Enter -> {
            onSubmit(password)
            true
          }

          Key.Escape -> {
            onDismiss()
            false
          }

          else -> false
        }
      }
    }
  if (show) {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Enter a password for $user") },
      modifier = Modifier.onKeyEvent(submit),
      text = {
        Box { PasswordInputField(password, { password = it }, Modifier.onKeyEvent(submit)) }
      },
      confirmButton = { Button(onClick = { onSubmit(password) }) { Text("Confirm") } },
      dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
  }
}

@Composable
private fun PasswordInputField(
  value: String,
  onChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var passwordVisibility: Boolean by remember { mutableStateOf(false) }
  OutlinedTextField(
    value = value,
    onValueChange = onChange,
    modifier = modifier.padding(top = 16.dp).fillMaxWidth(),
    label = { Text("Password") },
    placeholder = { Text(text = "Password") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    //      colors =
    //          TextFieldDefaults.outlinedTextFieldColors(
    //              //              focusedBorderColor = Color.Purple500,
    //              unfocusedBorderColor = Color.Black,
    //              placeholderColor = Color.Gray,
    //              textColor = Color.Black),
    singleLine = true,
    //      isError = true,
    visualTransformation =
    if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
    trailingIcon = {
      val image = if (passwordVisibility) Icons.Filled.Clear else Icons.Outlined.Edit
      IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
        Icon(imageVector = image, if (passwordVisibility) "hide-password" else "show-password")
      }
    }
  )
}
