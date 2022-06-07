```bash
sudo apt install v4l2loopback-dkms v4l2loopback-utils ffmpeg
sudo dnf -y install ffmpeg v4l2loopback

sudo apt install libopencv-dev # to build
sudo dnf install opencv-devel # to build

```

```bash
ls /dev | grep video
v4l2-ctl --list-devices
```

```bash
sudo modprobe v4l2loopback devices=1 card_label="My Fake Webcam" exclusive_caps=1 video_nr=40
sudo modprobe --remove v4l2loopback
```

```bash
ffmpeg -stream_loop -1 -re -i ~/Videos/sample.mpeg -vcodec rawvideo -threads 0 -f v4l2 /dev/video69
```
