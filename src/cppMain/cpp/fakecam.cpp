#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <iostream>
#include <linux/videodev2.h>
#include "fakecam.h"

// https://github.com/Harium/v4l2fakecam-java/blob/master/library/FakeCam.c
// More Formats at: https://linuxtv.org/downloads/v4l-dvb-apis/uapi/v4l/videodev.html
JNIEXPORT jint JNICALL Java_dev_petuska_fake_kamera_jni_FakeCam_open(JNIEnv *env, jobject obj, jstring device, jint width, jint height, jint channels)
{
    const char *output = env->GetStringUTFChars(device, 0);
    // open output device
    int dev_fd = open(output, O_RDWR);
    if (dev_fd < 0)
    {
        std::cerr << "ERROR: could not open output device!\n"
                  << &output
                  << strerror(errno);
        return -2;
    }

    // acquire video format from device
    struct v4l2_format vid_format;
    memset(&vid_format, 0, sizeof(vid_format));
    vid_format.type = V4L2_BUF_TYPE_VIDEO_OUTPUT;

    if (ioctl(dev_fd, VIDIOC_G_FMT, &vid_format) < 0)
    {
        std::cerr << "ERROR: unable to get video format!\n"
                  << strerror(errno);
        return -1;
    }

    // configure desired video format on device
    size_t framesize = width * height * channels;
    vid_format.fmt.pix.width = width;
    vid_format.fmt.pix.height = height;
    vid_format.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV;
    vid_format.fmt.pix.sizeimage = framesize;
    vid_format.fmt.pix.field = V4L2_FIELD_NONE;

    if (ioctl(dev_fd, VIDIOC_S_FMT, &vid_format) < 0)
    {
        std::cerr << "ERROR: unable to set video format!\n"
                  << strerror(errno);
        return -1;
    }

    env->ReleaseStringUTFChars(device, output);
    return dev_fd;

    //===========================================================================================

    // int dev_fd = open(deviceChar, O_WRONLY | O_SYNC);
    // if (dev_fd == -1)
    // {
    //     return dev_fd;
    // }

    // struct v4l2_format v;
    // v.type = V4L2_BUF_TYPE_VIDEO_OUTPUT;
    // if (ioctl(dev_fd, VIDIOC_G_FMT, &v) == -1)
    // {
    //     return -2;
    // }
    // v.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV;
    // v.fmt.pix.width = width;
    // v.fmt.pix.height = height;
    // v.fmt.pix.field = V4L2_FIELD_NONE;
    // //v.fmt.pix.field = V4L2_FIELD_ANY;
    // v.fmt.pix.bytesperline = width * channels;
    // v.fmt.pix.sizeimage = width * height * channels;
    // v.fmt.pix.colorspace = V4L2_COLORSPACE_JPEG;

    // if (ioctl(dev_fd, VIDIOC_S_FMT, &v) == -1)
    // {
    //     return -3;
    // }

    // env->ReleaseStringUTFChars(device, deviceChar);
    // return dev_fd;
}

JNIEXPORT jboolean JNICALL Java_dev_petuska_fake_kamera_jni_FakeCam_writeFrame(JNIEnv *env, jobject obj, jint dev_fd, jbyteArray frame)
{
    int frameSize = env->GetArrayLength(frame);

    jbyte *array = env->GetByteArrayElements(frame, NULL);
    char *frameBytes = (char *)array;

    int written = write(dev_fd, frameBytes, frameSize);

    env->ReleaseByteArrayElements(frame, array, 0);
    if (written < 0)
    {
        std::cerr << "ERROR: could not write to output device!\n";
        close(dev_fd);
        return written;
    }
    return written == frameSize;
}

JNIEXPORT jint JNICALL Java_dev_petuska_fake_kamera_jni_FakeCam_close(JNIEnv *env, jobject obj, jint dev_fd)
{
    return close(dev_fd);
}
