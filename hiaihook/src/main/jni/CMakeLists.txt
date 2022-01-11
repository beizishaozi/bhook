cmake_minimum_required(VERSION 3.18.1)
project(:hiaihook)

if(DEPENDENCY_ON_LOCAL_LIBRARY)
    # import bytehook from local-project
    string(TOLOWER ${CMAKE_BUILD_TYPE} BUILD_TYPE_DIRNAME)
    if(${BUILD_TYPE_DIRNAME} STREQUAL "relwithdebinfo")
        set(BUILD_TYPE_DIRNAME "release")
    endif()
    set(BYTEHOOK_BASE ${CMAKE_CURRENT_SOURCE_DIR}/../../../../bytehook)
    add_library(bytehook::bytehook SHARED IMPORTED)
    set_target_properties(bytehook::bytehook PROPERTIES
            IMPORTED_LOCATION ${BYTEHOOK_BASE}/build/intermediates/stripped_native_libs/${BUILD_TYPE_DIRNAME}/out/lib/${ANDROID_ABI}/libbytehook.so
            INTERFACE_INCLUDE_DIRECTORIES ${BYTEHOOK_BASE}/src/main/cpp/include)
else()
    # import bytehook from maven
    find_package(bytehook REQUIRED CONFIG)
endif()

add_library(libhiai SHARED IMPORTED )
set_target_properties(libhiai PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libhiai.so")

add_library(libcaffe SHARED IMPORTED )
set_target_properties(libcaffe PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libcaffe.so")

add_library(libcaffe_adapter SHARED IMPORTED )
set_target_properties(libcaffe_adapter PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libcaffe_adapter.so")

add_library(libtensorflow_adapter SHARED IMPORTED )
set_target_properties(libtensorflow_adapter PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libtensorflow_adapter.so")

add_library(libtensorflow_inference SHARED IMPORTED )
set_target_properties(libtensorflow_inference PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libtensorflow_inference.so")


# libhacker.so
add_library(hacker SHARED hacker.c)
target_compile_features(hacker PUBLIC c_std_11)
target_compile_options(hacker PUBLIC -Weverything -Werror)
target_link_libraries(hacker bytehook::bytehook log)

# libhookee.so
add_library(hookee SHARED hookee.c)
target_compile_features(hookee PUBLIC c_std_11)
target_compile_options(hookee PUBLIC -Weverything -Werror)
target_link_libraries(hookee log)

#libhiaijni.so
#include_directories(include)
add_library( # Sets the name of the library.
        hiaijni

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        mix_classify_jni.cpp
        mix_classify_async_jni.cpp
        mixbuildmodel.cpp
        relocation.cpp)
target_include_directories(hiaijni PRIVATE include)
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )
target_link_libraries( # Specifies the target library.
        hiaijni
        android
        ${libhiai}
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

