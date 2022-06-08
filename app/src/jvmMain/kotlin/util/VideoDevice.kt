package dev.petuska.fake.kamera.util

import org.kodein.di.bindings.ScopeCloseable
import java.io.Closeable

sealed interface VideoDevice : Closeable, ScopeCloseable {
  val path: String
  val opened: Boolean
  val width: Int
  val height: Int
  val channels: Int
  val fps: Int
  suspend fun open(fps: UInt? = null): Boolean
  fun toInput(channels: Int = 3): VideoDeviceInput
  fun toOutput(channels: Int = 2): VideoDeviceOutput
}
