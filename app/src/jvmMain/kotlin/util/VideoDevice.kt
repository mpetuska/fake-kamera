package dev.petuska.fake.kamera.util

sealed interface VideoDevice : AutoCloseable {
  val path: String
  val opened: Boolean
  val width: Int
  val height: Int
  val channels: Int
  suspend fun open(fps: UInt? = null): Boolean
  fun toInput(): VideoDeviceInput
  fun toOutput(): VideoDeviceOutput
}
