
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project(kbArt)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


aux_source_directory(src/main/cpp/art SRC_DIR_ART)
aux_source_directory(src/main/cpp/sliver SRC_DIR_Sliver)

add_library( # Sets the name of the library.
        kbArt

        # Sets the library as a shared library.
        SHARED
        ${SRC_DIR_ART}
        ${SRC_DIR_Sliver}
        # Provides a relative path to your source file(s).
        src/main/cpp/sliver.cpp
        src/main/cpp/art_vm.cpp
        src/main/cpp/art_thread_jni.cpp
        src/main/cpp/art_jni.cpp
        )

target_include_directories(kbArt PUBLIC
        src/main/cpp
        src/main/cpp/art
        src/main/cpp/sliver
        src/main/cpp/include)


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

find_package(bytehook REQUIRED CONFIG)
find_package(xdl REQUIRED CONFIG)
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        kbArt
        ${log-lib}
        xdl::xdl
        bytehook::bytehook)
