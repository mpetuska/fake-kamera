package dev.petuska.fake.kamera.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.petuska.fake.kamera.component.LazyScrollable
import dev.petuska.fake.kamera.util.logs
import org.kodein.log.Logger

@Composable
fun Logs() {
  val logs = logs
  LazyScrollable(
    itemCount = logs.size,
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .clip(RoundedCornerShape(10.dp))
      .border(1.dp, Color.Gray, RoundedCornerShape(10.dp)),
    title = "Logs",
    onClear = { logs.clear() }
  ) { i ->
    Box(
      modifier = Modifier.height(32.dp)
        .fillMaxWidth()
        .background(color = Color(0, 0, 0, 10))
        .padding(start = 10.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      logs[i].let { (tag, entry, message) ->
        Text(
          text = "[${entry.level}][$tag] $message",
          overflow = TextOverflow.Ellipsis,
          color = when (entry.level) {
            Logger.Level.DEBUG -> Color.Gray
            Logger.Level.INFO -> Color.Black
            Logger.Level.WARNING -> Color(248, 99, 29)
            Logger.Level.ERROR -> Color.Red
          }
        )
      }
    }
  }
}
