import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import theme.AppTheme
import ui.HomeScreen

@Composable
fun App() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Navigator(HomeScreen)
        }
    }
}