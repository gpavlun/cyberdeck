package dev.cyberdeck.ui

import dev.cyberdeck.MediaSessionBridge
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import android.location.LocationManager
import android.location.Location
import android.provider.Settings
import android.content.Intent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import dev.cyberdeck.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal val categories =
    listOf("COMMS", "MEDIA", "SOCIAL", "UTILS", "TOOLS", "SYSTEM", "APPS")

internal val Mono = FontFamily.Monospace

internal data class DeckPalette(
    val name: String,
    val background: Color,
    val accent: Color,
    val mutedAccent: Color,
    val border: Color,
    val surface: Color,
    val brightText: Color,
    val dimText: Color
)

internal val DeckPalettes = listOf(
    DeckPalette(
        name = "REDLINE",
        background = Color(0xFF10080B),
        accent = Color(0xFFFF304F),
        mutedAccent = Color(0xFFB52B43),
        border = Color(0xFF5C1827),
        surface = Color(0xFF160D11),
        brightText = Color(0xFFFFE9ED),
        dimText = Color(0xFF9B747D)
    ),
    DeckPalette(
        name = "CURVETRACER",
        background = Color(0xFF06110F),
        accent = Color(0xFF38FFB3),
        mutedAccent = Color(0xFF239C75),
        border = Color(0xFF176B52),
        surface = Color(0xFF0A1915),
        brightText = Color(0xFFE0FFF2),
        dimText = Color(0xFF729F8D)
    ),
    DeckPalette(
        name = "SYNTHWAVE",
        background = Color(0xFF090817),
        accent = Color(0xFFB69CFF),
        mutedAccent = Color(0xFF8063D9),
        border = Color(0xFF4931A8),
        surface = Color(0xFF100D23),
        brightText = Color(0xFFF0E9FF),
        dimText = Color(0xFF9183B8)
    ),
    DeckPalette(
        name = "COURSER",
        background = Color(0xFF080D18),
        accent = Color(0xFF55A7FF),
        mutedAccent = Color(0xFF3275C2),
        border = Color(0xFF174A85),
        surface = Color(0xFF0C1324),
        brightText = Color(0xFFE5F2FF),
        dimText = Color(0xFF7895B8)
    ),
    DeckPalette(
        name = "MONO",
        background = Color(0xFF101010),
        accent = Color(0xFFD0D0D0),
        mutedAccent = Color(0xFF929292),
        border = Color(0xFF505050),
        surface = Color(0xFF1A1A1A),
        brightText = Color(0xFFF0F0F0),
        dimText = Color(0xFF999999)
    ),
    DeckPalette(
        name = "CYBERPUNK",
        background = Color(0xFF0A0C18),
        accent = Color(0xFFFF4FA3),
        mutedAccent = Color(0xFFB83280),
        border = Color(0xFF285A8E),
        surface = Color(0xFF12182A),
        brightText = Color(0xFFF0F0F5),
        dimText = Color(0xFFE6C84A)
    ),
    DeckPalette(
        name = "MATRIX",
        background = Color(0xFF080F0A),
        accent = Color(0xFF65E68A),
        mutedAccent = Color(0xFF319653),
        border = Color(0xFF205C35),
        surface = Color(0xFF0F1A12),
        brightText = Color(0xFFE0F5E5),
        dimText = Color(0xFF80B890)
    ),
    DeckPalette(
        name = "GILDED",
        background = Color(0xFF120D06),
        accent = Color(0xFFFFBF5F),
        mutedAccent = Color(0xFFB88035),
        border = Color(0xFF68451F),
        surface = Color(0xFF1E160C),
        brightText = Color(0xFFFFF0D6),
        dimText = Color(0xFFC49A64)
    ),
    DeckPalette(
        name = "PHOSPHOR",
        background = Color(0xFF100904),
        accent = Color(0xFFFF8C32),
        mutedAccent = Color(0xFFB85B24),
        border = Color(0xFF663719),
        surface = Color(0xFF1C1109),
        brightText = Color(0xFFFFE5CC),
        dimText = Color(0xFFBA8156)
    )
)
