package dev.cyberdeck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeckBlack = Color(0xFF090607)
val PanelBlack = Color(0xFF14090C)


private val CyberdeckColors = darkColorScheme(
    primary = Color.White,
    secondary = Color.LightGray,
    background = DeckBlack,
    surface = PanelBlack,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)


@Composable
fun CyberdeckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberdeckColors,
        content = content
    )
}
