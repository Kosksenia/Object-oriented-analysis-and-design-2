cmake_minimum_required(VERSION 3.16)
project(WarehouseManager LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# ===== ВЕРСИЯ БЕЗ ПАТТЕРНА =====
add_executable(warehouse_bad 
    src/bad/main_bad.cpp
)

# ===== ВЕРСИЯ С ПАТТЕРНОМ =====
add_executable(warehouse_good 
    src/good/main_good.cpp
)

# Настройка для Windows GUI приложений
set_target_properties(warehouse_bad PROPERTIES 
    LINK_FLAGS "/SUBSYSTEM:WINDOWS"
)
set_target_properties(warehouse_good PROPERTIES 
    LINK_FLAGS "/SUBSYSTEM:WINDOWS"
)

# Подключение библиотек
target_link_libraries(warehouse_bad 
    comctl32 
    user32 
    gdi32
)
target_link_libraries(warehouse_good 
    comctl32 
    user32 
    gdi32
)

# Отключаем предупреждения о безопасности
target_compile_definitions(warehouse_bad PRIVATE 
    _CRT_SECURE_NO_WARNINGS
)
target_compile_definitions(warehouse_good PRIVATE 
    _CRT_SECURE_NO_WARNINGS
)

message(STATUS "Configuration complete")