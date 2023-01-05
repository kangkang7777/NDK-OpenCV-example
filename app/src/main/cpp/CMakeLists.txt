# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)


set(Boost_INCLUDE_DIR D://workplace//library//ndk_25_boost_1.79.0//include)
set(Boost_LIBRARIES D://workplace//library//ndk_25_boost_1.79.0//libs)

set(rpc_lib D://workplace//activeProject//NDK-OpenCV-example//app//src//main//java//org//restrpc//jni)

#find_package(Boost COMPONENTS system filesystem REQUIRED)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread -std=c++11")

include_directories(
        "include"
        "thirdparty"
        ${OpenCV_DIR}/jni/include
        ${Boost_INCLUDE_DIR}
)

add_library(lib_opencv SHARED IMPORTED)
add_library(restrpc_jni SHARED IMPORTED)

set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${OpenCV_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so)
set_target_properties(restrpc_jni PROPERTIES IMPORTED_LOCATION ${rpc_lib}/restrpc_jni.lib)



# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

add_library(boost_chrono SHARED IMPORTED)

add_library (boost_system SHARED IMPORTED)

set_target_properties(boost_chrono PROPERTIES IMPORTED_LOCATION ${Boost_LIBRARIES}/${ANDROID_ABI}/libboost_chrono.so)

set_target_properties(boost_system PROPERTIES IMPORTED_LOCATION ${Boost_LIBRARIES}/${ANDROID_ABI}/libboost_system.so)

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

target_link_libraries( # Specifies the target library.
        native-lib

        lib_opencv

        restrpc_jni
        
        boost_system

        boost_chrono
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})