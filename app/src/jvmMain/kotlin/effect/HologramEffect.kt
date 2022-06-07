package dev.petuska.fake.kamera.effect

import dev.petuska.fake.kamera.util.get
import dev.petuska.fake.kamera.util.set
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.dnn.Dnn
import org.opencv.imgproc.Imgproc
import kotlin.random.Random

object HologramEffect : FrameEffect {
  private val tf by lazy { Dnn.readNetFromTensorflow("frozen_inference_graph.pb", "graph.pbtxt") }

  private fun Mat.segment() {
    val blob = Dnn.blobFromImage(this)
    tf.setInput(blob)
    val result = tf.forward()
  }
  override fun invoke(frame: Mat): Mat =
    frame.clone().apply {
      if (!empty()) {
        tint()
//                    halftone()
//                    ghosting()
//                    Core.addWeighted(frame, 0.5, this, 0.6, 0.0, this)
      }
    }

  private fun Mat.tint() {
    Imgproc.cvtColor(this, this, Imgproc.COLORMAP_WINTER)
  }

  private fun Mat.halftone() {
    val bandLength = 2
    val bandGap = 3

    val rnd = Random(1)
    for (y in 0 until rows()) {
      if (y % (bandLength + bandGap) < bandLength) {
        // TODO this[y,:,:] = this[y,:,:] * (rnd.nextDouble(0.5) - 0.1)
        for (x in 0 until cols()) {
          for (c in 0 until this[y, x].size) {
            this[y, x, c] = this[y, x, c] * (rnd.nextDouble(0.5) - 0.1)
          }
        }
      }
    }
  }

  private fun Mat.ghosting() {
    Core.addWeighted(this, 0.2, this.clone().apply { shift(5, 5) }, 0.8, 0.0, this)
    Core.addWeighted(this, 0.4, this.clone().apply { shift(-5, -5) }, 0.6, 0.0, this)
  }

  private fun Mat.shift(dx: Int, dy: Int) {
    /* TODO
     # shift_img from: https://stackoverflow.com/a/53140617
     def shift_img(img, dx, dy):
         img = np.roll(img, dy, axis=0)
         img = np.roll(img, dx, axis=1)
         if dy>0:
             img[:dy, :] = 0
         elif dy<0:
             img[dy:, :] = 0
         if dx>0:
             img[:, :dx] = 0
         elif dx<0:
             img[:, dx:] = 0
         return img
    */
  }

  private fun Mat.blur() {
    Imgproc.blur(this, this, Size(30.0, 30.0))
  }

  private fun Mat.dilate() {
    val dKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(10.0, 10.0))
    Imgproc.dilate(this, this, dKernel, Point(-1.0, -1.0), 1)
  }
}
