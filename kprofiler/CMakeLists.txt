
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("kprofiler")


set(jvmagent_DIR src/main/cpp/jvmagent)
set(atrace_DIR src/main/cpp/atrace)

include_directories(src/main/cpp)
include_directories(src/main/cpp/jvmti)
include_directories(src/main/cpp/atrace/trace)
include_directories(src/main/cpp/atrace/utils)
add_subdirectory(${atrace_DIR} ${CMAKE_CURRENT_BINARY_DIR}/k-atrace)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        kprofiler

        # Sets the library as a shared library.
        SHARED
        ${JVM_AGENT}
        src/main/cpp/jvmagent/jvmti_helper.cpp
        src/main/cpp/jvmagent/agent_env.cpp
        # Provides a relative path to your source file(s).
        src/main/cpp/kprofiler.cpp
        src/main/cpp/kprofiler_JNI.cpp
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
find_package(kbArt REQUIRED CONFIG)
find_package(shadowhook REQUIRED CONFIG)
find_package(bytehook REQUIRED CONFIG)

target_link_libraries( # Specifies the target library.
        kprofiler

        # Links the target library to the log library
        # included in the NDK.
        bytehook::bytehook
        shadowhook::shadowhook
        kbArt::kbArt
        k-atrace
        ${log-lib})
