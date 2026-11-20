#include <jni.h>
extern "C" JNIEXPORT jint JNICALL
Java_com_lauda_lofi_audio_NativeDsp_nativeVersion(JNIEnv*, jobject) {
    return 1;
}
