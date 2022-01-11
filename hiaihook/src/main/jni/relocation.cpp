#include <jni.h>
#include <stdio.h>
#include <dlfcn.h>
#include <stdlib.h>
#include <string>
//
// Created by 张燕 on 2021/12/30.
//
typedef char*(*p_strcat)(unsigned int dest, char *src);

extern "C"
JNIEXPORT void JNICALL
Java_com_huawei_itrustee_hiaihook_utils_ModelManager_startlocation(JNIEnv *env, jclass clazz) {
    // TODO: implement startlocation()
    void *handle;

    handle = dlopen("/system/lib64/vndk-sp-29/libhidlbase.so", RTLD_LAZY);
    p_strcat pstrcat = (p_strcat)dlsym(handle,"_ZN7android8hardware10BpHwBinder8transactEjRKNS0_6ParcelEPS2_jNSt3__18functionIFvRS2_EEE");
    printf("p_strcat=%p,*p_strcat=%p\n",pstrcat,*pstrcat);
}