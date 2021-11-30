#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/videodev2.h>
#include "fakecam.h"

// https://github.com/Harium/v4l2fakecam-java/blob/master/library/FakeCam.c
// More Formats at: https://linuxtv.org/downloads/v4l-dvb-apis/uapi/v4l/videodev.html
JNIEXPORT jint JNICALL Java_dev_petuska_fake_kamera_jni_FakeCam_open(JNIEnv* env, jobject obj, jstring device, jint width, jint height, jint channels)
{
    const char *deviceChar = env->GetStringUTFChars(device, 0);

    int dev_fd = open(deviceChar, O_WRONLY | O_SYNC);
    if (dev_fd == -1) {
      return dev_fd;
    }

   struct v4l2_format v;
    v.type = V4L2_BUF_TYPE_VIDEO_OUTPUT;
    if (ioctl(dev_fd, VIDIOC_G_FMT, &v) == -1) {
        return -2;
    }
    v.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV;
    v.fmt.pix.width = width;
    v.fmt.pix.height = height;
    v.fmt.pix.field = V4L2_FIELD_NONE;
    //v.fmt.pix.field = V4L2_FIELD_ANY;
    v.fmt.pix.bytesperline = width * channels;
    v.fmt.pix.sizeimage = width * height * channels;
    v.fmt.pix.colorspace =  V4L2_COLORSPACE_JPEG;

    if (ioctl(dev_fd, VIDIOC_S_FMT, &v) == -1) {
        return -3;
    }

    env->ReleaseStringUTFChars(device, deviceChar);
    return dev_fd;
}

JNIEXPORT jboolean JNICALL Java_dev_petuska_fake_kamera_jni_FakeCam_writeFrame(JNIEnv* env, jobject obj, jint dev_fd, jbyteArray frame)
{
  int frameSize = env->GetArrayLength(frame);

  jbyte* array = env->GetByteArrayElements(frame, 0);
  char* frameBytes = (char*) array;

  int result = write(dev_fd, frameBytes, frameSize);

  env->ReleaseByteArrayElements(frame, array, 0);
  return result == frameSize;
}

JNIEXPORT jint JNICALL Java_dev_petuska_fake_kamera_jni_FakeCam_close(JNIEnv* env, jobject obj, jint dev_fd)
{
    return close(dev_fd);
}
