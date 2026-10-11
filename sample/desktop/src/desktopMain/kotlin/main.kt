import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose DnD",
        // Phone-sized by default so desktop recordings match the mobile layout.
        state = rememberWindowState(width = 430.dp, height = 900.dp),
    ) {
        App()
    }
}
