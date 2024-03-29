package dev.petuska.fake.kamera.effect

import org.opencv.core.Mat

fun interface FrameEffect : (Mat) -> Mat {
  object NoOp : FrameEffect {
    override fun invoke(frame: Mat): Mat = frame
  }

  operator fun plus(other: FrameEffect): FrameEffect = FrameEffect {
    this(it)
    other(it)
  }
}
