cmake_minimum_required(VERSION 3.6)
project(ssim)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES main.cpp libs.cpp ssim.cpp ssim.h mjpegreader.h mlib.h)
add_executable(ssim ${SOURCE_FILES})
