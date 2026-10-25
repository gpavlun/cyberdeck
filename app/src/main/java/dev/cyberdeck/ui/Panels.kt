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

@Composable
internal fun Panel(
    title: String,
    palette: DeckPalette,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(CutCornerShape(5.dp))
            .background(palette.surface)
            .border(1.dp, palette.border, CutCornerShape(5.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            title,
            color = palette.accent,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.border)
        )

        if (scrollable) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                content()
            }
        } else {
            content()
        }
    }
}


@Composable
internal fun SimplePanel(
    palette: DeckPalette,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(CutCornerShape(5.dp))
            .background(palette.surface)
            .border(1.dp, palette.border, CutCornerShape(5.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        content()
    }
}


@Composable
private fun StatusLine(palette: DeckPalette, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = palette.dimText, fontSize = 8.sp, fontFamily = Mono)
        Text(value, color = palette.accent, fontSize = 8.sp, fontFamily = Mono)
    }
}


@Composable
internal fun PlayerButton(label: String, palette: DeckPalette, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(CutCornerShape(3.dp))
            .background(palette.background)
            .border(1.dp, palette.border, CutCornerShape(3.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = palette.accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


private fun Color.darken(factor: Float): Color =
    copy(
        red = red * factor,
        green = green * factor,
        blue = blue * factor
    )
