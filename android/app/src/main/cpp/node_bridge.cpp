#include <jni.h>
#include <android/log.h>
#include <cerrno>
#include <cstdlib>
#include <cstring>
#include <string>
#include <unistd.h>
#include <vector>

#include "node.h"
#include "node_version.h"

#define LOG_TAG "OpenWA-Node"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

std::string jstringToUtf8(JNIEnv* env, jstring value) {
    if (value == nullptr) {
        return {};
    }
    const char* chars = env->GetStringUTFChars(value, nullptr);
    std::string out = chars == nullptr ? "" : chars;
    if (chars != nullptr) {
        env->ReleaseStringUTFChars(value, chars);
    }
    return out;
}

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_org_rasitech_openwacompanion_engine_NodeBridge_nativeNodeCompileVersion(
        JNIEnv* env,
        jclass /* clazz */) {
    // NODE_VERSION_STRING is compile-time pin from embedded headers.
    return env->NewStringUTF(NODE_VERSION_STRING);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_rasitech_openwacompanion_engine_NodeBridge_nativeStartNodeWithArguments(
        JNIEnv* env,
        jclass /* clazz */,
        jobjectArray arguments,
        jstring workingDirectory) {
    if (arguments == nullptr) {
        ALOGE("startNode: null arguments");
        return -1;
    }

    if (workingDirectory != nullptr) {
        const std::string cwd = jstringToUtf8(env, workingDirectory);
        if (!cwd.empty() && ::chdir(cwd.c_str()) != 0) {
            ALOGE("startNode: chdir(%s) failed errno=%d", cwd.c_str(), errno);
            return -1;
        }
    }

    const jsize argumentCount = env->GetArrayLength(arguments);
    if (argumentCount <= 0) {
        ALOGE("startNode: empty arguments");
        return -1;
    }

    std::vector<std::string> owned;
    owned.reserve(static_cast<size_t>(argumentCount));
    for (jsize i = 0; i < argumentCount; ++i) {
        auto arg = reinterpret_cast<jstring>(env->GetObjectArrayElement(arguments, i));
        owned.emplace_back(jstringToUtf8(env, arg));
        if (arg != nullptr) {
            env->DeleteLocalRef(arg);
        }
    }

    size_t bytes = 0;
    for (const auto& s : owned) {
        bytes += s.size() + 1;
    }

    char* buffer = static_cast<char*>(std::calloc(bytes, sizeof(char)));
    if (buffer == nullptr) {
        ALOGE("startNode: calloc failed");
        return -1;
    }

    std::vector<char*> argv;
    argv.reserve(owned.size());
    char* cursor = buffer;
    for (const auto& s : owned) {
        std::memcpy(cursor, s.c_str(), s.size());
        cursor[s.size()] = '\0';
        argv.push_back(cursor);
        cursor += s.size() + 1;
    }

    ALOGI("starting node argc=%zu entry=%s", argv.size(), argv.empty() ? "?" : argv[0]);
    const int code = node::Start(static_cast<int>(argv.size()), argv.data());
    ALOGI("node::Start returned %d", code);
    std::free(buffer);
    return static_cast<jint>(code);
}
