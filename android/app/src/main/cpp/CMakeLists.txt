cmake_minimum_required(VERSION 3.18.1)

project("whisper_android")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_STANDARD 11)

# Optimization and required flags
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -DNDEBUG")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -DNDEBUG")

# Disable building of examples and tests from whisper.cpp
set(WHISPER_BUILD_TESTS OFF CACHE BOOL "whisper: build tests" FORCE)
set(WHISPER_BUILD_EXAMPLES OFF CACHE BOOL "whisper: build examples" FORCE)
set(WHISPER_BUILD_SERVER OFF CACHE BOOL "whisper: build server example" FORCE)

# Add whisper as a subdirectory
add_subdirectory(whisper.cpp)

# Add include directories for our android main
include_directories(
    whisper.cpp/include
)

add_library(
    whisper_android
    SHARED
    whisper_android.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    whisper_android
    whisper
    ${log-lib}
    dl
)
