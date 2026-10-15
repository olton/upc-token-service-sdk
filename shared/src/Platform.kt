package ua.upc.ecomm_sdk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
