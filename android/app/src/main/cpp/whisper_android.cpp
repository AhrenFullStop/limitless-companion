#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "whisper.cpp/include/whisper.h"

#define LOG_TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static void whisper_log_callback_impl(enum ggml_log_level level, const char * text, void * user_data) {
    if (level == GGML_LOG_LEVEL_ERROR) {
        LOGE("%s", text);
    } else {
        LOGI("%s", text);
    }
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_limitless_companion_services_WhisperService_nativeInitialize(
    JNIEnv* env,
    jobject /* this */,
    jstring modelPath,
    jboolean useGPU) {
    
    whisper_log_set(whisper_log_callback_impl, nullptr);

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    LOGI("Initializing whisper from %s", path);
    
    struct whisper_context_params cparams = whisper_context_default_params();
    cparams.use_gpu = useGPU;
    
    struct whisper_context* ctx = whisper_init_from_file_with_params(path, cparams);
    env->ReleaseStringUTFChars(modelPath, path);
    
    if (ctx == nullptr) {
        LOGE("Failed to initialize whisper context");
        return 0;
    }
    
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_limitless_companion_services_WhisperService_nativeTranscribe(
    JNIEnv* env,
    jobject /* this */,
    jlong contextPtr,
    jfloatArray audioData,
    jstring language,
    jint threads) {
    
    if (contextPtr == 0) return nullptr;
    
    struct whisper_context* ctx = reinterpret_cast<struct whisper_context*>(contextPtr);
    
    jsize audioLen = env->GetArrayLength(audioData);
    jfloat* audioElements = env->GetFloatArrayElements(audioData, nullptr);
    
    whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    wparams.print_progress = false;
    wparams.print_special = false;
    wparams.print_realtime = false;
    wparams.print_timestamps = false;
    wparams.n_threads = threads;
    
    if (language != nullptr) {
        const char* langStr = env->GetStringUTFChars(language, nullptr);
        wparams.language = langStr;
        // The language pointer must survive whisper_full, so we copy it or release it after
    } else {
        wparams.language = "en"; // Or let it auto-detect if "auto"
    }

    const char* langStr = nullptr;
    if (language != nullptr) {
        langStr = env->GetStringUTFChars(language, nullptr);
    }
    
    if (whisper_full(ctx, wparams, audioElements, audioLen) != 0) {
        LOGE("Failed to run whisper transcription");
        env->ReleaseFloatArrayElements(audioData, audioElements, JNI_ABORT);
        if (langStr != nullptr) env->ReleaseStringUTFChars(language, langStr);
        return nullptr;
    }
    
    env->ReleaseFloatArrayElements(audioData, audioElements, JNI_ABORT);
    if (langStr != nullptr) env->ReleaseStringUTFChars(language, langStr);
    
    int n_segments = whisper_full_n_segments(ctx);
    std::string full_text = "";
    
    // We should build the TranscriptionResult, but for now just returning the combined text 
    // is simpler, though the function signature asks for TranscriptionResult jobject.
    // Let's manually invoke the constructor of TranscriptionResult.
    
    for (int i = 0; i < n_segments; ++i) {
        const char* text = whisper_full_get_segment_text(ctx, i);
        full_text += text;
    }
    
    // Construct com.limitless.companion.services.TranscriptionResult
    jclass resultClass = env->FindClass("com/limitless/companion/services/TranscriptionResult");
    jmethodID constructor = env->GetMethodID(resultClass, "<init>", "(Ljava/lang/String;FLjava/lang/String;Ljava/util/List;J)V");
    
    jstring jText = env->NewStringUTF(full_text.c_str());
    jstring jLang = env->NewStringUTF(wparams.language);
    
    jobject resultObj = env->NewObject(resultClass, constructor, jText, 1.0f, jLang, nullptr, (jlong)0);
    return resultObj;
}

extern "C" JNIEXPORT void JNICALL
Java_com_limitless_companion_services_WhisperService_nativeRelease(
    JNIEnv* env,
    jobject /* this */,
    jlong contextPtr) {
    if (contextPtr != 0) {
        struct whisper_context* ctx = reinterpret_cast<struct whisper_context*>(contextPtr);
        whisper_free(ctx);
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_limitless_companion_services_WhisperService_nativeGetVersion(
    JNIEnv* env,
    jobject /* this */) {
    return env->NewStringUTF(whisper_print_system_info());
}
