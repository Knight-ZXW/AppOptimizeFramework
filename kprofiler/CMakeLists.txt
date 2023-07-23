
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("kprofiler")

set(utils_DIR src/main/cpp/utils)
set(jvmagent_DIR src/main/cpp/jvmagent)
set(trace_DIR src/main/cpp/trace)
set(fastlog_DIR src/main/cpp/third_party/profilo/fastlog)


include_directories(src/main/cpp)
include_directories(src/main/cpp/jvmti)
include_directories(${utils_DIR})
include_directories(${trace_DIR})
include_directories(${fastlog_DIR})


aux_source_directory(${utils_DIR} SRC_DIR_UTILS)
aux_source_directory(${jvmagent_DIR} JVM_AGENT)
aux_source_directory(${trace_DIR} SRC_DIR_TRACE)

add_subdirectory(${fastlog_DIR} ${CMAKE_CURRENT_BINARY_DIR}/fastlog)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        kprofiler

        # Sets the library as a shared library.
        SHARED
        ${SRC_DIR_UTILS}
        ${JVM_AGENT}
        ${SRC_DIR_TRACE}
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
        fastlog
        ${log-lib})
