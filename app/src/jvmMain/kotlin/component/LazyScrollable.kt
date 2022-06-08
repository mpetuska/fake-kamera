package dev.petuska.fake.kamera.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.petuska.fake.kamera.util.rememberMutableStateOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyScrollable(
  itemCount: Int,
  modifier: Modifier = Modifier,
  title: String? = null,
  onClear: (() -> Unit)? = null,
  render: @Composable (Int) -> Unit
) {
  Box(
    modifier = modifier
//      .background(color = Color(180, 180, 180))
      .padding(10.dp)
  ) {
    val vState = rememberLazyListState()
    val vAdapter = rememberScrollbarAdapter(scrollState = vState)

    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(end = 12.dp),
      state = vState,
      verticalArrangement = Arrangement.Top,
    ) {
      stickyHeader {
        Box(
          modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
        ) {
          if (title != null) {
            Text(
              text = title,
              fontWeight = FontWeight.Bold,
              fontSize = 1.25.em,
              modifier = Modifier.padding(6.dp).align(Alignment.CenterStart)
            )
          }
          if (onClear != null) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "clear",
              modifier = Modifier.padding(6.dp).clip(CircleShape).clickable(onClick = onClear)
                .align(Alignment.CenterEnd)
            )
          }
        }
      }
      items(itemCount) {
        render(it)
        Spacer(modifier = Modifier.height(5.dp))
      }
    }
    var oldItemCount by rememberMutableStateOf<Int?> { null }
    LaunchedEffect(Unit) {
      vState.scrollToItem(itemCount)
    }
    LaunchedEffect(itemCount) {
      val last = vState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
      if (last == oldItemCount || last == oldItemCount?.plus(1)) {
        vState.scrollToItem(itemCount + 1)
      }
      oldItemCount = itemCount
    }
    VerticalScrollbar(
      modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
      adapter = vAdapter,
    )
  }
}
