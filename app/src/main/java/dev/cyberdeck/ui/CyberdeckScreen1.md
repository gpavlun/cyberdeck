
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

private val categories =
    listOf("COMMS", "MEDIA", "SOCIAL", "UTILS", "TOOLS", "SYSTEM", "APPS")

private val Mono = FontFamily.Monospace

private data class DeckPalette(
    val name: String,
    val background: Color,
    val accent: Color,
    val mutedAccent: Color,
    val border: Color,
    val surface: Color,
    val brightText: Color,
    val dimText: Color
)

private val DeckPalettes = listOf(
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







@Composable
fun CyberdeckScreen(
    onToggleSystemUi: () -> Unit,
    onOpenMediaAccessSettings: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("STATUS") }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var terminalInput by remember { mutableStateOf("") }
    var terminalOutput by remember { mutableStateOf("LOCAL NODE READY") }
    var selectedPaletteIndex by remember { mutableIntStateOf(3) }
    val palette = DeckPalettes[selectedPaletteIndex]

    val context = LocalContext.current

    val mediaInfo = MediaSessionBridge.info
    val mediaController = MediaSessionBridge.controller

    val appCategoryPreferences = remember(context) {
        context.getSharedPreferences(
            "cyberdeck_app_categories",
            android.content.Context.MODE_PRIVATE
        )
    }

    var appCategories by remember {
        mutableStateOf(loadAppCategories(appCategoryPreferences))
    }

    var installedApps by remember {
        mutableStateOf<List<LaunchableApp>>(emptyList())
    }

    LaunchedEffect(Unit) {
        installedApps = withContext(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            context.packageManager
                .queryIntentActivities(intent, 0)
                .mapNotNull { info ->
                    val packageName = info.activityInfo?.packageName
                        ?: return@mapNotNull null

                    LaunchableApp(
                        name = info.loadLabel(context.packageManager).toString(),
                        packageName = packageName
                    )
                }
                .distinctBy { it.packageName }
                .sortedBy { it.name.lowercase() }
        }
    }


    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000)
        }
    }

    val toggleAppCategory: (String, String) -> Unit =
        { packageName, category ->
            val updated = appCategories[packageName]
                .orEmpty()
                .toMutableSet()

            if (category in updated) {
                updated.remove(category)
            } else {
                updated.add(category)
            }

            appCategories = if (updated.isEmpty()) {
                appCategories - packageName
            } else {
                appCategories + (packageName to updated.toSet())
            }

            appCategoryPreferences.edit().apply {
                if (updated.isEmpty()) {
                    remove(packageName)
                } else {
                    putStringSet(packageName, updated.toSet())
                }
            }.apply()
        }

    val displayedApps = if (selectedCategory == "APPS") {
        installedApps
    } else {
        installedApps.filter { app ->
            selectedCategory in appCategories[app.packageName].orEmpty()
        }
    }


    val time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val date = now.format(DateTimeFormatter.ofPattern("MMM dd EEE")).uppercase()
    val sysdate = now.format(DateTimeFormatter.ofPattern("YYYY.MM.dd"))

    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black, Color.Black),//palette.border.darken(0.5f), Color.Black),
                        center = Offset(size.width, 0f),
                        radius = size.maxDimension * 1.2f
                    )
                )
            }
            .padding(7.dp)
            // .border(1.dp, palette.border, CutCornerShape(12.dp))
            // .padding(7.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedPaletteIndex =
                            (selectedPaletteIndex + 1) % DeckPalettes.size
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable {
                        selectedPaletteIndex =
                            (selectedPaletteIndex + 1) % DeckPalettes.size
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CYBERDECK::${palette.name}",
                        color = palette.accent,
                        fontSize = 9.sp,
                        fontFamily = Mono,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "● ONLINE",
                        color = palette.accent,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            
            // Clock
            SimplePanel(
                palette = palette
            ) {
                Spacer(Modifier.width(2.dp))
                Row(
                    Modifier.fillMaxWidth().height(58.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = time,
                        color = palette.brightText,
                        fontSize = 40.sp,
                        fontFamily = Mono,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-2).sp,
                        lineHeight = 42.sp,
                        maxLines = 1
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = date,
                            color = palette.accent,
                            fontSize = 14.sp,
                            fontFamily = Mono,
                            letterSpacing = 1.sp,
                            lineHeight = 16.sp,
                            maxLines = 1
                        )
                        Text(
                            text = sysdate,
                            color = palette.mutedAccent,
                            fontSize = 10.sp,
                            fontFamily = Mono,
                            letterSpacing = 1.sp,
                            lineHeight = 12.sp,
                            maxLines = 1
                        )
                    }
                }
                
            }
           

            // Compact music player — no artwork.
            SimplePanel(
                palette = palette
            ) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = mediaInfo.title.ifBlank {
                            if (mediaInfo.hasSession) "UNKNOWN TRACK"
                            else "NO ACTIVE MEDIA SESSION"
                        },
                        color = palette.brightText,
                        fontSize = 16.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = mediaInfo.artist.ifBlank { "UNKNOWN ARTIST" },
                        color = palette.dimText,
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val progress = if (mediaInfo.durationMs > 0L) {
                            (mediaInfo.positionMs.toFloat() /
                                mediaInfo.durationMs).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        Box(
                            Modifier
                                .weight(1f)
                                .height(9.dp)
                                .background(palette.background)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(palette.accent)
                            )
                        }

                        Text(
                            "${formatTime(mediaInfo.positionMs)} / " +
                                if (mediaInfo.durationMs > 0L) {
                                    formatTime(mediaInfo.durationMs)
                                } else {
                                    "--:--"
                                },
                            color = palette.dimText,
                            fontSize = 7.sp,
                            fontFamily = Mono
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) {
                            PlayerButton("◂◂", palette = palette) {
                                mediaController?.transportControls
                                    ?.skipToPrevious()
                            }
                        }

                        Box(Modifier.weight(1f)) {
                            PlayerButton(
                                if (mediaInfo.isPlaying) "Ⅱ" else "▶",
                                palette = palette
                            ) {
                                val controls =
                                    mediaController?.transportControls

                                if (mediaInfo.isPlaying) {
                                    controls?.pause()
                                } else {
                                    controls?.play()
                                }
                            }
                        }

                        Box(Modifier.weight(1f)) {
                            PlayerButton("▸▸", palette = palette) {
                                mediaController?.transportControls
                                    ?.skipToNext()
                            }
                        }
                    }
                
            }


            // Quick access
            Panel(
                title = "MODE: ${palette.name}",
                palette = palette,
                modifier = Modifier.clickable {
                    selectedPaletteIndex =
                        (selectedPaletteIndex + 1) % DeckPalettes.size
                }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CALL", "SMS", "WEB", "DOCK").forEach { label ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(CutCornerShape(4.dp))
                                .background(palette.background)
                                .border(1.dp, palette.border, CutCornerShape(4.dp))
                                .clickable {
                                    when (label) {
                                        "SMS" -> {
                                            val intent = Intent(
                                                Intent.ACTION_SENDTO,
                                                Uri.parse("smsto:")
                                            )
                                            context.startActivity(intent)
                                        }

                                        "CALL" -> {
                                            val intent = Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.parse("tel:")
                                            )
                                            context.startActivity(intent)
                                        }

                                        "WEB" -> {
                                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                                addCategory(Intent.CATEGORY_APP_BROWSER)
                                            }

                                            try {
                                                context.startActivity(intent)
                                            } catch (e: android.content.ActivityNotFoundException) {
                                                context.startActivity(
                                                    Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("https://www.google.com")
                                                    )
                                                )
                                            }
                                        }

                                        "DOCK" -> onToggleSystemUi()

                                        else -> {
                                            terminalOutput =
                                                "$label // INTEGRATION PENDING"
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = palette.accent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // Status and category rail, aligned at the top.
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (selectedCategory == "STATUS") {
                    Panel(
                        title = "SYSTEM STATUS",
                        modifier = Modifier.fillMaxHeight(),
                        scrollable = false,
                        palette = palette
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // System telemetry section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(bottom = 6.dp)
                            ) {
                                SystemTelemetry(palette)
                            }

                            // Terminal output section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(1.dp, palette.border)
                                    .padding(6.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = terminalOutput.ifBlank { "AWAITING INPUT..." },
                                    color = if (terminalOutput.isBlank()) {
                                        palette.dimText
                                    } else {
                                        palette.brightText
                                    },
                                    fontSize = 9.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                    } else {
                        InstalledAppsPanel(
                            palette = palette,
                            apps = displayedApps,
                            appCategories = appCategories,
                            onToggleCategory = toggleAppCategory,
                            title = if (selectedCategory == "APPS") {
                                "[ ${displayedApps.size} ] APPLICATIONS"
                            } else {
                                "[ ${displayedApps.size} ] $selectedCategory"
                            },
                            showCategoryButton = selectedCategory == "APPS"
                        )
                    }
                }

                Column(
                    Modifier
                        .width(72.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        val active = selectedCategory == category

                        Box(
                            Modifier
                                .fillMaxWidth()
                                //.height(27.dp)
                                .weight(1f)
                                .clip(CutCornerShape(4.dp))
                                .border(1.dp, palette.border, CutCornerShape(3.dp))
                                .background(
                                    if (active) palette.mutedAccent
                                    else palette.background
                                )
                                .border(
                                    1.dp,
                                    if (active) palette.accent else palette.background,
                                    CutCornerShape(4.dp)
                                )
                                .clickable {
                                    selectedCategory =
                                        if (active) "STATUS" else category
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                category,
                                color = if (active) palette.brightText else palette.accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // CLI

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(CutCornerShape(5.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.background, CutCornerShape(5.dp))
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SYS:~$",
                    color = palette.accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Mono
                )
                Spacer(Modifier.width(7.dp))

                BasicTextField(
                    value = terminalInput,
                    onValueChange = { terminalInput = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = palette.brightText,
                        fontFamily = Mono,
                        fontSize = 10.sp
                    ),
                    cursorBrush = Brush.verticalGradient(
                        listOf(palette.accent, palette.accent)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            terminalOutput = runDemoCommand(terminalInput)
                            terminalInput = ""
                        }
                    ),
                    decorationBox = { inner ->
                        Box {
                            if (terminalInput.isEmpty()) {
                                Text(
                                    "enter command...",
                                    color = palette.dimText,
                                    fontSize = 10.sp,
                                    fontFamily = Mono
                                )
                            }
                            inner()
                        }
                    }
                )

                Text(
                    "↵",
                    modifier = Modifier
                        .clickable {
                            terminalOutput = runDemoCommand(terminalInput)
                            terminalInput = ""
                        }
                        .padding(start = 8.dp),
                    color = palette.accent,
                    fontSize = 13.sp
                )
            }
            

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "BUILD 0004",
                    color = palette.dimText,
                    fontSize = 7.sp,
                    letterSpacing = 0.5.sp
                )
                // Text(
                //     "NIGHT://CALL",
                //     color = palette.accent,
                //     fontSize = 7.sp
                // )
                Box(
                    modifier = Modifier
                        .clickable {
                            context.startActivity(
                                Intent(Settings.ACTION_SETTINGS)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SETTINGS",
                        color = palette.accent,
                        fontSize = 7.sp,
                        fontFamily = Mono,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

            }
        }
    }
}



@Composable
private fun Panel(
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
private fun SimplePanel(
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
private fun PlayerButton(label: String, palette: DeckPalette, onClick: () -> Unit) {
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


private fun runDemoCommand(input: String): String {
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



@Composable
private fun SystemTelemetry(palette: DeckPalette) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var batteryLevel by remember { mutableIntStateOf(0) }
    var charging by remember { mutableStateOf(false) }
    var wifiConnected by remember { mutableStateOf(false) }
    var cellularConnected by remember { mutableStateOf(false) }
    var bluetoothEnabled by remember { mutableStateOf(false) }
    var bluetoothPermission by remember { mutableStateOf(false) }
    var wifiRssi by remember { mutableIntStateOf(-127) }

    var locationEnabled by remember { mutableStateOf(false) }
    var locationPermission by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    var cellSignalDbm by remember { mutableIntStateOf(-999) }
    var cellSignalLevel by remember { mutableIntStateOf(-1) }

    var phoneStatePermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // Open a system settings screen, with a general settings fallback.
    fun openSettings(
        action: String,
        fallback: String = android.provider.Settings.ACTION_SETTINGS
    ) {
        try {
            context.startActivity(android.content.Intent(action))
        } catch (_: Exception) {
            try {
                context.startActivity(android.content.Intent(fallback))
            } catch (_: Exception) {
                // Leave the current screen usable if no handler exists.
            }
        }
    }

    // Location permission.
    val locationPermissionLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts
                .RequestMultiplePermissions()
        ) { permissions ->
            locationPermission =
                permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }

    // Cellular signal permission.
    val phonePermissionLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { granted ->
            phoneStatePermission = granted
        }

    // Bluetooth permission and enable request.
    val bluetoothPermissionLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { granted ->
            bluetoothPermission = granted

            if (granted) {
                val manager = context.getSystemService(
                    android.content.Context.BLUETOOTH_SERVICE
                ) as android.bluetooth.BluetoothManager

                if (manager.adapter?.isEnabled != true) {
                    try {
                        context.startActivity(
                            android.content.Intent(
                                android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
                            )
                        )
                    } catch (_: Exception) {
                        openSettings(
                            android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
                        )
                    }
                }
            }
        }

    // Bluetooth interaction.
    val onBluetoothTap = {
        val hasPermission =
            android.os.Build.VERSION.SDK_INT < 31 ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        bluetoothPermission = hasPermission

        if (!hasPermission) {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                bluetoothPermissionLauncher.launch(
                    android.Manifest.permission.BLUETOOTH_CONNECT
                )
            }
        } else {
            val manager = context.getSystemService(
                android.content.Context.BLUETOOTH_SERVICE
            ) as android.bluetooth.BluetoothManager

            if (manager.adapter?.isEnabled == true) {
                openSettings(
                    android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
                )
            } else {
                try {
                    context.startActivity(
                        android.content.Intent(
                            android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
                        )
                    )
                } catch (_: Exception) {
                    openSettings(
                        android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
                    )
                }
            }
        }
    }

    // Location interaction.
    val onLocationTap = {
        if (!locationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            openSettings(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
        }
    }

    // Android's Internet panel exposes Wi-Fi and mobile data controls.
    val onInternetTap = {
        openSettings(
            android.provider.Settings.Panel.ACTION_INTERNET_CONNECTIVITY,
            android.provider.Settings.ACTION_WIFI_SETTINGS
        )
    }

    // Request cellular signal permission if it has not been granted yet.
    LaunchedEffect(Unit) {
        if (!phoneStatePermission) {
            phonePermissionLauncher.launch(
                android.Manifest.permission.READ_PHONE_STATE
            )
        }
    }

    // Read the existing location permission state and request it if needed.
    LaunchedEffect(Unit) {
        val fineGranted =
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        locationPermission = fineGranted || coarseGranted

        if (!locationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Periodically update location and coordinate readings.
    LaunchedEffect(locationPermission) {
        if (!locationPermission) {
            locationEnabled = false
            latitude = null
            longitude = null
            return@LaunchedEffect
        }

        val manager = context.getSystemService(
            android.content.Context.LOCATION_SERVICE
        ) as android.location.LocationManager

        var lastLocationRequest = 0L

        while (true) {
            locationEnabled = try {
                manager.isProviderEnabled(
                    android.location.LocationManager.GPS_PROVIDER
                ) || manager.isProviderEnabled(
                    android.location.LocationManager.NETWORK_PROVIDER
                )
            } catch (_: Exception) {
                false
            }

            if (!locationEnabled) {
                latitude = null
                longitude = null
            } else {
                val now = android.os.SystemClock.elapsedRealtime()

                if (now - lastLocationRequest >= 30_000L) {
                    lastLocationRequest = now

                    val provider = listOf(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        android.location.LocationManager.GPS_PROVIDER
                    ).firstOrNull { name ->
                        try {
                            manager.isProviderEnabled(name)
                        } catch (_: Exception) {
                            false
                        }
                    }

                    if (provider != null) {
                        try {
                            manager.getCurrentLocation(
                                provider,
                                null,
                                context.mainExecutor
                            ) { location ->
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                }
                            }
                        } catch (_: SecurityException) {
                            latitude = null
                            longitude = null
                        } catch (_: IllegalArgumentException) {
                            latitude = null
                            longitude = null
                        }
                    }
                }
            }

            kotlinx.coroutines.delay(5_000L)
        }
    }

    // Poll system telemetry.
    LaunchedEffect(context) {
        while (true) {
            // Battery.
            val batteryIntent = context.registerReceiver(
                null,
                android.content.IntentFilter(
                    android.content.Intent.ACTION_BATTERY_CHANGED
                )
            )

            batteryIntent?.let { intent ->
                val level = intent.getIntExtra("level", -1)
                val scale = intent.getIntExtra("scale", -1)

                if (level >= 0 && scale > 0) {
                    batteryLevel = level * 100 / scale
                }

                val status = intent.getIntExtra("status", -1)
                charging =
                    status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL
            }

            // Active network state.
            val connectivity = context.getSystemService(
                android.content.Context.CONNECTIVITY_SERVICE
            ) as android.net.ConnectivityManager

            val capabilities = connectivity.getNetworkCapabilities(
                connectivity.activeNetwork
            )

            wifiConnected = capabilities?.hasTransport(
                android.net.NetworkCapabilities.TRANSPORT_WIFI
            ) == true

            cellularConnected = capabilities?.hasTransport(
                android.net.NetworkCapabilities.TRANSPORT_CELLULAR
            ) == true

            // Wi-Fi signal.
            try {
                val wifiManager =
                    context.applicationContext.getSystemService(
                        android.content.Context.WIFI_SERVICE
                    ) as android.net.wifi.WifiManager

                val rssi = wifiManager.connectionInfo.rssi

                wifiRssi = if (wifiConnected && rssi in -100..-1) {
                    rssi
                } else {
                    -127
                }
            } catch (_: Exception) {
                wifiRssi = -127
            }

            // Bluetooth state.
            val hasBluetoothPermission =
                android.os.Build.VERSION.SDK_INT < 31 ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            bluetoothPermission = hasBluetoothPermission

            if (hasBluetoothPermission) {
                try {
                    val manager = context.getSystemService(
                        android.content.Context.BLUETOOTH_SERVICE
                    ) as android.bluetooth.BluetoothManager

                    bluetoothEnabled = manager.adapter?.isEnabled == true
                } catch (_: SecurityException) {
                    bluetoothEnabled = false
                }
            } else {
                bluetoothEnabled = false
            }

            // Cellular signal strength.
            if (phoneStatePermission) {
                try {
                    val telephonyManager = context.getSystemService(
                        android.content.Context.TELEPHONY_SERVICE
                    ) as android.telephony.TelephonyManager

                    val signal = telephonyManager.signalStrength
                    cellSignalLevel = signal?.level ?: -1

                    val dbm = signal
                        ?.cellSignalStrengths
                        ?.firstOrNull()
                        ?.dbm

                    cellSignalDbm = if (dbm != null && dbm < 0) {
                        dbm
                    } else {
                        -999
                    }
                } catch (_: SecurityException) {
                    cellSignalDbm = -999
                    cellSignalLevel = -1
                }
            } else {
                cellSignalDbm = -999
                cellSignalLevel = -1
            }

            kotlinx.coroutines.delay(3000L)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Battery + Wi-Fi.
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TelemetryTile(
                palette = palette,
                label = "BATTERY",
                value = "$batteryLevel%",
                detail = if (charging) "CHARGING" else "ON BATTERY",
                active = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        openSettings(
                            android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS
                        )
                    },
                accentValue = true
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(palette.background)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((batteryLevel / 100f).coerceIn(0f, 1f))
                            .background(palette.accent)
                    )
                }
            }

            TelemetryTile(
                palette = palette,
                label = "WIFI",
                value = when {
                    !wifiConnected -> "OFFLINE"
                    wifiRssi == -127 -> "CONNECTED"
                    else -> "$wifiRssi dBm"
                },
                detail = when {
                    !wifiConnected -> "NO CONNECTION"
                    wifiRssi == -127 -> "SIGNAL UNKNOWN"
                    else -> "SIGNAL STRENGTH"
                },
                active = wifiConnected,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onInternetTap() },
                extra = {
                    val signalFraction =
                        if (wifiConnected && wifiRssi != -127) {
                            ((wifiRssi + 100) / 50f).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                    val filledSegments = (signalFraction * 10).toInt()

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(10) { index ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (index < filledSegments) {
                                            palette.accent
                                        } else {
                                            palette.background
                                        }
                                    )
                            )
                        }
                    }
                }
            )
        }

        // Bluetooth + Location.
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TelemetryTile(
                palette = palette,
                label = "BLUETOOTH",
                value = when {
                    !bluetoothPermission -> "NO PERMISSION"
                    bluetoothEnabled -> "ENABLED"
                    else -> "DISABLED"
                },
                detail = "RADIO STATUS",
                active = bluetoothPermission && bluetoothEnabled,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onBluetoothTap() }
            )

            TelemetryTile(
                palette = palette,
                label = "LOCATION",
                value = when {
                    !locationPermission -> "NO PERMISSION"
                    !locationEnabled -> "DISABLED"
                    latitude != null && longitude != null -> "ENABLED"
                    else -> "ACQUIRING..."
                },
                detail = if (latitude != null && longitude != null) {
                    "%.4f, %.4f".format(
                        java.util.Locale.US,
                        latitude,
                        longitude
                    )
                } else {
                    "GPS / NETWORK"
                },
                active = locationEnabled && latitude != null,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLocationTap() }
            )
        }

        // Cellular signal and connectivity controls.
        TelemetryTile(
            palette = palette,
            label = "CELLULAR",
            value = when {
                !phoneStatePermission -> "NO PERMISSION"
                cellSignalDbm != -999 -> "$cellSignalDbm dBm"
                cellSignalLevel >= 0 -> "LEVEL $cellSignalLevel / 4"
                else -> "SIGNAL UNAVAILABLE"
            },
            detail = when {
                !phoneStatePermission -> "ACCESS DENIED"
                cellularConnected -> "MOBILE DATA ACTIVE"
                else -> "STANDBY"
            },
            active = phoneStatePermission &&
                cellSignalDbm != -999 &&
                cellSignalDbm >= -100,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onInternetTap() },
            extra = {
                val validSignal =
                    phoneStatePermission && cellSignalDbm != -999

                // -120 dBm = 0 segments; -70 dBm = 10 segments.
                val filledSegments = if (validSignal) {
                    ((cellSignalDbm + 120) / 5f + 0.5f)
                        .toInt()
                        .coerceIn(0, 10)
                } else {
                    0
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(10) { index ->
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    if (index < filledSegments) {
                                        palette.accent
                                    } else {
                                        palette.background
                                    }
                                )
                        )
                    }
                }
            }
        )
    }
}






@Composable
private fun TelemetryTile(
    palette: DeckPalette,
    label: String,
    value: String,
    detail: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    accentValue: Boolean = false,
    extra: (@Composable () -> Unit)? = null
) {
    Column(
        modifier
            .background(palette.surface)
            .border(1.dp, palette.border)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ){
        Text(
            text = label,
            color = palette.dimText,
            fontSize = 7.sp,
            lineHeight = 8.sp,
            fontFamily = Mono,
            maxLines = 1
        )

        Text(
            text = value,
            color = when {
                accentValue -> palette.accent
                active -> palette.brightText
                else -> palette.dimText
            },
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Mono,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = detail,
            color = palette.dimText,
            fontSize = 7.sp,
            lineHeight = 8.sp,
            fontFamily = Mono,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (extra != null) {
            Spacer(Modifier.height(1.dp))
            extra()
            Spacer(Modifier.height(1.dp))
        }
    }
}



private data class LaunchableApp(
    val name: String,
    val packageName: String
)


@Composable
private fun InstalledAppsPanel(
    palette: DeckPalette,
    apps: List<LaunchableApp>,
    appCategories: Map<String, Set<String>>,
    onToggleCategory: (String, String) -> Unit,
    title: String,
    showCategoryButton: Boolean
){
    val context = LocalContext.current
    var expandedApp by remember { mutableStateOf<String?>(null) }

    val categories = categories

    Panel(
        title = title,
        palette = palette,
        scrollable = false,
        modifier = Modifier.fillMaxHeight()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (apps.isEmpty()) {
                item {
                    Text(
                        text = "NO APPS ASSIGNED",
                        color = palette.dimText,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

            items(
                items = apps,
                key = { it.packageName }
            ) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(4.dp))
                        .background(palette.background)
                        .border(
                            1.dp,
                            palette.border,
                            CutCornerShape(4.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                context.packageManager
                                    .getLaunchIntentForPackage(
                                        app.packageName
                                    )
                                    ?.let(context::startActivity)
                            }
                            .padding(
                                horizontal = 10.dp,
                                vertical = 10.dp
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = app.name,
                            color = palette.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if(showCategoryButton){
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        expandedApp =
                                            if (expandedApp == app.packageName) {
                                                null
                                            } else {
                                                app.packageName
                                            }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    color = palette.accent,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            DropdownMenu(
                                expanded = expandedApp == app.packageName,
                                onDismissRequest = {
                                    expandedApp = null
                                }
                            ) {
                                categories.forEach { category ->
                                    val assigned =
                                        category in appCategories[
                                            app.packageName
                                        ].orEmpty()

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (assigned) {
                                                    "✓  $category"
                                                } else {
                                                    category
                                                },
                                                color = palette.accent
                                            )
                                        },
                                        onClick = {
                                            onToggleCategory(
                                                app.packageName,
                                                category
                                            )
                                            expandedApp = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun loadAppCategories(
    preferences: SharedPreferences
): Map<String, Set<String>> {
    return preferences.all.mapNotNull { (packageName, value) ->
        val categories = (value as? Set<*>)
            ?.filterIsInstance<String>()
            ?.toSet()

        if (categories.isNullOrEmpty()) {
            null
        } else {
            packageName to categories
        }
    }.toMap()
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d".format(minutes, seconds)
}