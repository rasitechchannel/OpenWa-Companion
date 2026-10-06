cmake_minimum_required(VERSION 3.22.1)
project(openwa_node_bridge)

add_library(openwa-node SHARED node_bridge.cpp)

# cpp -> main -> src -> app
set(OPENWA_LIBNODE_ROOT ${CMAKE_SOURCE_DIR}/../../../libnode)

target_include_directories(openwa-node PRIVATE
    ${OPENWA_LIBNODE_ROOT}/include/node
)

add_library(libnode SHARED IMPORTED)
set_target_properties(libnode PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libnode.so
)

find_library(log-lib log)

target_link_libraries(openwa-node libnode ${log-lib})
