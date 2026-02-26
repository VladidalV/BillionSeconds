package com.example.billionseconds

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform