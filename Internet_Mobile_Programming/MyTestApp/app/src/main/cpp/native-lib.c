#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <math.h>
#include <termios.h>
#include <sys/mman.h>
#include <jni.h>

#define TEXTLCD_ON          1
#define TEXTLCD_OFF         2
#define TEXTLCD_INIT        3
#define TEXTLCD_CLEAR       4
#define TEXTLCD_LINE1       5
#define TEXTLCD_LINE2       6

JNIEXPORT void JNICALL
Java_org_example_mytestapp_DeviceControl_segmentWrite(
        JNIEnv* env,
        jobject thiz, jint num) {
    int dev;

    dev = open("/dev/fpga_segment", O_WRONLY, S_IRWXU);

    if (dev > 0) {
        write(dev, &num, sizeof(num));
        close(dev);
        usleep(2000);
    }
    else
        exit(1);
}

JNIEXPORT void JNICALL
Java_org_example_mytestapp_DeviceControl_segmentIOctl(
        JNIEnv* env,
        jobject thiz, jint num) {
    int dev;

    dev = open("/dev/fpga_segment", O_RDWR | O_SYNC, S_IRWXU);

    if (dev > 0) {
        ioctl(dev, num, NULL, NULL);
        close(dev);
    }
    else
        exit(1);
}

JNIEXPORT void JNICALL
Java_org_example_mytestapp_DeviceControl_ledWrite(JNIEnv *env, jobject thiz) {
    int dev;
    int bit[] = {0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0xFF};

    dev = open("/dev/fpga_led", O_WRONLY, S_IRWXU);

    if (dev > 0) {
        for (int i = 1; i < 9; i++) {
            write(dev, bit+i, 1);
            usleep(100000);
        }
        for (int i = 8; i >= 1; i--) {
            write(dev, bit+i, 1);
            usleep(100000);
        }
        write(dev, bit+9, 1);
        sleep(1);
        write(dev, bit+0, 1);
        close(dev);
    }
    else
        exit(1);
}

JNIEXPORT void JNICALL
Java_org_example_mytestapp_DeviceControl_dotMatrixWrite(JNIEnv *env, jobject thiz, jstring data) {
    int dev, len;
    const char *buf;

    buf = (*env)->GetStringUTFChars(env, data, 0);
    len = (*env)->GetStringLength(env, data);

    dev = open("/dev/fpga_dotmatrix", O_WRONLY, S_IRWXU);

    if (dev > 0) {
        write(dev, buf, len);
        close(dev);
    }
    else
        exit(1);
}

JNIEXPORT void JNICALL
Java_org_example_mytestapp_DeviceControl_textWrite(JNIEnv *env, jobject thiz, jboolean isClear) {
    int dev;
    const char *clear_buf1 = "CONGLATURATION!";
    const char *clear_buf2 = "YOU WIN";
    const char *fail_buf1 = "TOO BAD!";
    const char *fail_buf2 = "YOU LOOSE";

    dev = open("/dev/fpga_textlcd", O_WRONLY, S_IRWXU);

    if (dev > 0) {
        ioctl(dev, TEXTLCD_INIT);
        ioctl(dev, TEXTLCD_CLEAR);

        if (isClear) {
            ioctl(dev, TEXTLCD_LINE1);
            write(dev, clear_buf1, strnlen(clear_buf1, 20));
            ioctl(dev, TEXTLCD_LINE2);
            write(dev, clear_buf2, strnlen(clear_buf2, 20));
        }
        else {
            ioctl(dev, TEXTLCD_LINE1);
            write(dev, fail_buf1, strnlen(fail_buf1, 20));
            ioctl(dev, TEXTLCD_LINE2);
            write(dev, fail_buf2, strnlen(fail_buf2, 20));
        }

        ioctl(dev, TEXTLCD_OFF);
        close(dev);
    }
    else
        exit(1);
}

JNIEXPORT void JNICALL
Java_org_example_mytestapp_DeviceControl_textClear(JNIEnv *env, jobject thiz) {
    int dev;

    dev = open("/dev/fpga_textlcd", O_WRONLY, S_IRWXU);

    if (dev > 0) {
        ioctl(dev, TEXTLCD_INIT);
        ioctl(dev, TEXTLCD_CLEAR);

        ioctl(dev, TEXTLCD_OFF);
        close(dev);
    }
    else
        exit(1);
}