package com.example.composedemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform