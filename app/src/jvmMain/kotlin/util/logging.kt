package dev.petuska.fake.kamera.util

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.kodein.log.*
import org.kodein.log.frontend.defaultLogFrontend

data class LogMessage(
  val tag: Logger.Tag,
  val entry: Logger.Entry,
  val message: String
)

data class WithLogFactory<T>(val data: T, val factory: LoggerFactory)

fun <T> LoggerFactory.data(data: T): WithLogFactory<T> {
  return WithLogFactory(data, this)
}

class UILogFrontend(val onMessage: (LogMessage) -> Unit) : LogFrontend {
  override fun getReceiverFor(tag: Logger.Tag): LogReceiver {
    return LogReceiver { entry, message ->
      message?.let { onMessage(LogMessage(tag, entry, it)) }
    }
  }
}

private val LogLocal = staticCompositionLocalOf<SnapshotStateList<LogMessage>> { error("undefined") }
val LoggerFactoryLocal = staticCompositionLocalOf<LoggerFactory> { error("undefined") }
private val LoggerLocal = staticCompositionLocalOf<Logger> { error("undefined") }

@Composable
fun LogProvider(content: @Composable () -> Unit) {
  val log = remember { mutableStateListOf<LogMessage>() }
  val factory = remember(log) {
    LoggerFactory(
      defaultLogFrontend,
      UILogFrontend {
        log.add(it)
      }
    )
  }
  val defaultLogger = remember { factory.newLogger("", "UI") }
  CompositionLocalProvider(
    LogLocal provides log,
    LoggerFactoryLocal provides factory,
    LoggerLocal provides defaultLogger,
    content = content
  )
}

@Composable
inline fun <reified T : Any> logger(): Logger {
  val factory = LoggerFactoryLocal.current
  return remember(factory) { factory.newLogger<T>() }
}

@Composable
fun logger(name: String, pkg: String = "ui"): Logger {
  val factory = LoggerFactoryLocal.current
  return remember(factory) { factory.newLogger(pkg, name) }
}

val logger: Logger
  @Composable
  get() = LoggerLocal.current

val logs: MutableList<LogMessage>
  @Composable
  get() = LogLocal.current
