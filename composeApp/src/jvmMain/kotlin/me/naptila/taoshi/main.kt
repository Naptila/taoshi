package me.naptila.taoshi

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        title = "淘师湾自动作业",
        onCloseRequest = ::exitApplication,
        resizable = false,
        state = rememberWindowState(position = WindowPosition.Aligned(Alignment.Center), width = 320.dp, height = 448.dp)
    ) {
        App()
    }
}