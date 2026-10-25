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

internal fun runDemoCommand(input: String): String {
    return when (input.trim().lowercase()) {
        "" -> "NO COMMAND"
        "help" -> "HELP STATUS DATE TIME APPS CLEAR"
        "status" -> "LOCAL NODE READY"
        "date" -> LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("EEE dd MMM"))
            .uppercase()
        "time" -> LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        "apps" -> "APP INDEX OFFLINE"
        "whoami" -> "OPERATOR // LOCAL"
        "clear" -> "LOCAL NODE READY"
        else -> "DEMO ONLY // UNKNOWN COMMAND"
    }
}
