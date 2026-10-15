package ua.upc.ecomm_sdk

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ecomm-sdk") {
        App()
    }
}
