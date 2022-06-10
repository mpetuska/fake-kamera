package dev.petuska.fake.kamera.util

import org.opencv.core.Core
import org.opencv.core.CvType.CV_32F
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class BodypixScaler(
  val width: Int,
  val height: Int,
  val type: Int,
) {
  private val zeroes = Mat.zeros(height, width, type)
  private val ones = Mat.ones(height, width, type)

  fun scaleAndCropToInputTensorShape(tensor: Mat, applySigmoidActivation: Boolean): Mat {
    val inResizedAndPadded = Mat { Imgproc.resize(tensor, it, Size(width.toDouble(), height.toDouble())) }
    zeroes.print("zeroes")
    inResizedAndPadded.print("inResizedAndPadded")
    return if (applySigmoidActivation) {
      sigmoid(inResizedAndPadded)
    } else {
      inResizedAndPadded
    }
  }

  private fun sigmoid(src: Mat): Mat {
    val subbed = Mat { Core.subtract(zeroes, src, it) }
    val exp = Mat { Core.exp(subbed, it) }
    val added = Mat { Core.add(ones, exp, it) }
    return Mat { Core.divide(ones, added, it, 1.0, CV_32F) }
  }
}
