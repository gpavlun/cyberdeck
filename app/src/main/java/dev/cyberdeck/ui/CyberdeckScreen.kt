package dev.cyberdeck.ui

import dev.cyberdeck.MediaSessionBridge
import dev.cyberdeck.MediaInfo
import android.media.session.MediaController
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
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha




@Composable
fun CyberdeckScreen(
    onToggleSystemUi: () -> Unit,
    onOpenMediaAccessSettings: () -> Unit,
    selectedPaletteIndex: Int,
    onPaletteChange: (Int) -> Unit,
    terminalOutput: String,
    onTerminalOutput: (String) -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenConfiguration: () -> Unit,
    callInProgress: Boolean
) {

    var selectedCategory by remember { mutableStateOf("STATUS") }
    var now by remember { mutableStateOf(LocalDateTime.now()) }

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

    val railPreferences = remember(context) {
        context.getSharedPreferences(
            "cyberdeck_rail_assignments",
            android.content.Context.MODE_PRIVATE
        )
    }

    var selectedRailOption by remember {
        mutableStateOf("VOICE LINK")
    }

    var railAssignments by remember {
        mutableStateOf(
            RailCategories.associateWith { category ->
                railPreferences.getString(
                    category,
                    DEFAULT_RAIL_ASSIGNMENT
                ) ?: DEFAULT_RAIL_ASSIGNMENT
            }
        )
    }

    val assignRailApp: (String, String) -> Unit = { category, packageName ->
        railAssignments = railAssignments + (category to packageName)

        railPreferences.edit()
            .putString(category, packageName)
            .apply()
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
                        colors = listOf(Color.Black, Color.Black),
                        center = Offset(size.width, 0f),
                        radius = size.maxDimension * 1.2f
                    )
                )
            }
            .padding(7.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
                /*>>>>> START LAYOUT <<<<<*/

                HeaderModule(
                    palette = palette,
                    onCyclePalette = {
                        onPaletteChange(
                            (selectedPaletteIndex + 1) % DeckPalettes.size
                        )
                    }
                )

                Box(Modifier.fillMaxWidth().height(1.dp).background(palette.border))

                ClockModule(
                    palette = palette,
                    time = time,
                    date = date,
                    sysdate = sysdate
                )

                when{
                    callInProgress -> CallModule(
                        palette = palette,
                    )
                    mediaInfo.hasSession == true -> MusicPlayerModule(
                        palette = palette,
                        mediaInfo = mediaInfo,
                        mediaController = mediaController
                    )
                    else -> Spacer(Modifier.height(64.dp))
                }
  
                Spacer(Modifier.height(32.dp))

                StatusCategoryModule(
                    palette = palette,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    categories = categories,
                    displayedApps = displayedApps,
                    appCategories = appCategories,
                    onToggleCategory = toggleAppCategory,
                    railAssignments = railAssignments,
                    onConfigureRailOption = onOpenConfiguration
                )     

                CliModule(
                    palette = palette,
                    onOpenTerminal = onOpenTerminal
                )
                
                FooterModule(
                    palette = palette,
                    onOpenConfiguration = onOpenConfiguration
                )

                /*>>>>> END LAYOUT <<<<<*/
        }
    }
}


@Composable
private fun ClockModule(
    palette: DeckPalette,
    time: String,
    date: String,
    sysdate: String
) {
    // SimplePanel(
    //     palette = palette
    // ) {
        Spacer(Modifier.width(2.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .height(58.dp),
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
    //}
}

@Composable
private fun HeaderModule(
    palette: DeckPalette,
    onCyclePalette: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onCyclePalette),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.clickable(
                onClick = onCyclePalette
            ),
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

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "● ONLINE",
                color = palette.accent,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun MusicPlayerModule(
    palette: DeckPalette,
    mediaInfo: MediaInfo,
    mediaController: MediaController?
) {

    val context = LocalContext.current

    val openMediaApp = {
        mediaController?.let { controller ->
            val intent = context.packageManager
                .getLaunchIntentForPackage(controller.packageName)

            intent?.let { context.startActivity(it) }
        }
    }


    SimplePanel(
        palette = palette,
        modifier = Modifier.clickable {
            openMediaApp()
        }
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
                    val controls = mediaController?.transportControls

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
}




@Composable
private fun ColumnScope.StatusCategoryModule(
    palette: DeckPalette,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    categories: List<String>,
    displayedApps: List<LaunchableApp>,
    appCategories: Map<String, Set<String>>,
    onToggleCategory: (String, String) -> Unit,
    railAssignments: Map<String, String>,
    onConfigureRailOption: () -> Unit
) {
    val context = LocalContext.current
    var showAppCategories by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Central panel: keep existing status and app-list behavior.
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            if (selectedCategory == "STATUS") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val stroke = 1.dp.toPx()
                        val inset = stroke / 2f

                        val left = inset
                        val top = inset
                        val right = size.width - inset
                        val bottom = size.height - inset

                        val notchWidth = 15.dp.toPx()
                        val notchHeight = 15.dp.toPx()
                        val notchOffset = 0.25f * size.height - 2.0f * notchHeight
                        val notchLength = size.height - 2.0f * notchOffset - 4.0f * notchHeight
                        val slantWidth = 10.dp.toPx()

                        val shape = Path().apply {
                            //back edge
                            moveTo(left + notchWidth, top)
                            lineTo(right, top)
                            lineTo(right, bottom)
                            lineTo(left + notchWidth, bottom)
                            //start notch
                            lineTo(left, bottom - notchHeight)
                            lineTo(left, bottom - notchHeight - notchOffset)
                            lineTo(left + notchWidth, bottom - notchHeight - notchOffset - notchHeight)
                            lineTo(left + notchWidth, bottom - notchHeight - notchOffset - notchHeight - notchLength)
                            lineTo(left, bottom - notchHeight - notchOffset - notchHeight - notchLength - notchHeight)
                            lineTo(left, top + notchHeight)
                            close()
                        }

                        drawPath(shape, color = palette.background)
                        drawPath(
                            shape,
                            color = palette.border,
                            style = Stroke(width = stroke)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                            //.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "SYSTEM STATUS",
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

                        SystemTelemetry(palette)
                    }
                }
            } else {
                InstalledAppsPanel(
                    palette = palette,
                    apps = displayedApps,
                    appCategories = appCategories,
                    onToggleCategory = onToggleCategory,
                    title = if (selectedCategory == "APPS") {
                        "[ ${displayedApps.size} ] APPLICATIONS"
                    } else {
                        "[ ${displayedApps.size} ] $selectedCategory"
                    },
                    showCategoryButton = selectedCategory == "APPS"
                )
            }
        }

        // Right rail: quick access mode or app-category mode.
        Column(
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!showAppCategories) {
                RailCategories.forEach { category ->
                    val assignment = railAssignments[category]
                        ?: DEFAULT_RAIL_ASSIGNMENT

                    val target = remember(category, assignment, context) {
                        resolveRailTarget(
                            context = context,
                            category = category,
                            assignment = assignment
                        )
                    }

                    RailButton(
                        palette = palette,
                        title = category,
                        subtitle = target?.label?.let { "SYS // $it" } ?: "[ NOT ASSIGNED ]",
                        active = false,
                        onClick = {
                            if (target == null) {
                                onConfigureRailOption()
                            } else {
                                try {
                                    context.startActivity(target.intent)
                                } catch (_: Exception) {
                                    onConfigureRailOption()
                                }
                            }
                        }
                    )
                }

                RailButton(
                    palette = palette,
                    title = "APPS",
                    subtitle = "APPLICATIONS",
                    active = false,
                    onClick = { 
                        onSelectCategory("APPS")
                        showAppCategories = true 
                    }
                )
            } else {
                categories.forEach { category ->
                    val active = selectedCategory == category

                    if (category == "APPS") {
                        RailButton(
                            active = selectedCategory == category,
                            title = "APPS",
                            subtitle = "APPLICATIONS",
                            palette = palette,
                            onClick = {
                                if (selectedCategory == category) {
                                    onSelectCategory("STATUS")
                                    showAppCategories = false
                                } else {
                                    onSelectCategory(category)
                                }
                            }
                        )
                    } else {
                        CategoryFilterButton(
                            category = category,
                            selected = selectedCategory == category,
                            palette = palette,
                            onClick = {
                                if (selectedCategory == category) {
                                    onSelectCategory("STATUS")
                                    showAppCategories = false
                                } else {
                                    onSelectCategory(category)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class RailTarget(
    val intent: Intent,
    val label: String
)

private fun resolveRailTarget(
    context: android.content.Context,
    category: String,
    assignment: String
): RailTarget? {
    val intent = if (assignment != DEFAULT_RAIL_ASSIGNMENT) {
        // An explicitly assigned app must have a launchable activity.
        context.packageManager.getLaunchIntentForPackage(assignment)
            ?: return null
    } else {
        // Device defaults for the six quick-access options.
        when (category) {
            "VOICE LINK" -> Intent(Intent.ACTION_DIAL)

            "COMMS" -> Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
            }

            "OPTICS" -> Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE
            )

            "NETWORK" -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://example.com")
            )

            "AUDIO" -> Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
            }

            "LOGS" -> Intent("android.intent.action.CREATE_NOTE")

            else -> return null
        }
    }

    return try {
        val resolved = context.packageManager.resolveActivity(intent, 0)
            ?: return null

        val label = if (assignment != DEFAULT_RAIL_ASSIGNMENT) {
            val appInfo = context.packageManager.getApplicationInfo(
                assignment,
                0
            )
            context.packageManager.getApplicationLabel(appInfo).toString()
        } else {
            resolved.loadLabel(context.packageManager).toString()
        }

        RailTarget(intent, label)
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun ColumnScope.RailButton(
    palette: DeckPalette,
    title: String,
    subtitle: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            //.height(54.dp)
            .weight(1f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 1.dp.toPx()
            val inset = stroke / 2f

            val left = inset
            val top = inset
            val right = size.width - inset
            val bottom = size.height - inset

            val notchWidth = 30.dp.toPx()
            val notchHeight = 35.dp.toPx()
            val slantWidth = 10.dp.toPx()

            val shape = Path().apply {
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right, bottom)
                lineTo(left + notchWidth + slantWidth, bottom)
                lineTo(left + notchWidth, top + notchHeight)
                lineTo(left, top + notchHeight)
                close()
            }

            drawPath(
                path = shape,
                color = if (active) palette.surface else Color.Black
            )

            drawPath(
                path = shape,
                color = if (active) palette.accent else palette.border,
                style = Stroke(width = stroke)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 12.dp,
                    end = 7.dp,
                    top = 5.dp,
                    bottom = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(1.dp))

            Text(
                text = title,
                color = if (active) palette.brightText else palette.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = if (active) palette.brightText else palette.dimText,
                fontSize = 7.sp,
                fontFamily = Mono,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "◈",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 5.dp, bottom = 28.dp),
            color = if (active) palette.accent else palette.border,
            fontSize = 25.sp
        )
    }
}

@Composable
private fun ColumnScope.CategoryFilterButton(
    category: String,
    selected: Boolean,
    palette: DeckPalette,
    onClick: () -> Unit
) {

    val shape = CutCornerShape(
    topStart = 10.dp,
    topEnd = 0.dp,
    bottomEnd = 10.dp,
    bottomStart = 0.dp
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(shape)
            .background(
                if (selected) palette.surface
                else palette.background
            )
            .border(
                width = 1.dp,
                color = if (selected) palette.accent
                else palette.border,
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            color = if (selected) palette.accent
            else palette.dimText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}




@Composable
private fun ColumnScope.CliModule(
    palette: DeckPalette,
    onOpenTerminal: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(CutCornerShape(5.dp))
            .background(palette.surface)
            .border(1.dp, palette.background, CutCornerShape(5.dp))
            .clickable(onClick = onOpenTerminal)
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

        Text(
            "enter command...",
            modifier = Modifier.weight(1f),
            color = palette.dimText,
            fontSize = 10.sp,
            fontFamily = Mono
        )

        Text(
            "↵",
            color = palette.accent,
            fontSize = 13.sp
        )
    }
}


@Composable
private fun FooterModule(
    palette: DeckPalette,
    onOpenConfiguration: () -> Unit
) {
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

        Box(
            modifier = Modifier.clickable {
                onOpenConfiguration()
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CONFIGURATION",
                color = palette.accent,
                fontSize = 7.sp,
                fontFamily = Mono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun CallModule(
    palette: DeckPalette
) {
    val infiniteTransition = rememberInfiniteTransition(label = "call_indicator")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 650,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "call_dot_alpha"
    )

    SimplePanel(palette = palette) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(dotAlpha)
                    .background(palette.accent, CircleShape)
            )

            Text(
                text = "VOICE LINK ACTIVE",
                color = palette.brightText,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Mono,
                maxLines = 1
            )
        }
    }
}