package dev.petuska.fake.kamera.store

import dev.petuska.fake.kamera.effect.FrameEffect
import dev.petuska.fake.kamera.effect.HologramEffect
import dev.petuska.fake.kamera.effect.plus

data class AppState(
  val devices: List<String>,
  val inputDevicePath: String? = null,
  val outputDevicePath: String? = null,
  val fps: UInt = 60u,
  val effect: FrameEffect = FrameEffect.NoOp + HologramEffect
)
