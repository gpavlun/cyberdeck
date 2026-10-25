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

internal data class LaunchableApp(
    val name: String,
    val packageName: String
)


@Composable
internal fun InstalledAppsPanel(
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


internal fun loadAppCategories(
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

internal fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d".format(minutes, seconds)
}
