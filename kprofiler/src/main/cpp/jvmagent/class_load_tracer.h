//
// Created by Knight-ZXW on 2023/4/14.
//

#pragma once

#include <jni.h>
#include <mutex>
#include "jvmti.h"



class ClassLoadTracer{

public:
    ClassLoadTracer(const char * filePath);
    ~ClassLoadTracer();
    void recordMsg(const char * msg);
    void stop();

    static void setCurTracer(ClassLoadTracer*);
    static ClassLoadTracer* getTracer();
    static JNIEnv* getEnv();
    static void JNICALL ClassPrepareCallback(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jclass clazz);
    jobject targetThread;

    int32_t attachJvmti(jvmtiEnv *pEnv);

private:
    std::atomic_flag mutex_;
    FILE* writeFile;
    std::string  buffer;


};