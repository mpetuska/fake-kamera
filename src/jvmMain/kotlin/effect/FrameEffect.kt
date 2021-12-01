package dev.petuska.fake.kamera.effect

import kotlin.random.Random
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

fun interface FrameEffect : (Mat) -> Mat {
  object NoOp : FrameEffect {
    override fun invoke(frame: Mat): Mat = frame
  }
}

object HologramEffect : FrameEffect {
  override fun invoke(frame: Mat): Mat =
      frame.clone().apply {
        if (!empty()) {
          dilate()
          blur()
          beams()
          shift()
          Core.addWeighted(frame, 0.5, this, 0.6, 0.0, this)
        }
      }

  private fun Mat.shift() {
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

     # the first one is roughly: holo * 0.2 + shifted_holo * 0.8 + 0
     holo2 = cv2.addWeighted(holo, 0.2, shift_img(holo1.copy(), 5, 5), 0.8, 0)
     holo2 = cv2.addWeighted(holo2, 0.4, shift_img(holo1.copy(), -5, -5), 0.6, 0)
    */
  }

  private fun Mat.beams() {
    Imgproc.applyColorMap(this, this, Imgproc.COLORMAP_WINTER)
    //    Imgproc.applyColorMap(this, this, Imgproc.COLORMAP_DEEPGREEN)
    val bandLength = 2
    val bandGap = 3

    val rnd = Random(1)
    for (y in 0 until height()) {
      if (y % (bandLength + bandGap) < bandLength) {
        for (x in 0 until width()) {
          // TODO this[y,:,:] = this[y,:,:] * (rnd.nextDouble(0.5) - 0.1)
          for (c in 0 until this[y, x].size) {
//            this[x, y, c] = this[x, y, c] * (rnd.nextDouble(0.5) - 0.1)
          }
        }
      }
    }
  }

  private fun Mat.blur() {
    Imgproc.blur(this, this, Size(30.0, 30.0))
  }

  private fun Mat.dilate() {
    val dKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(10.0, 10.0))
    Imgproc.dilate(this, this, dKernel, Point(-1.0, -1.0), 1)
  }
}
