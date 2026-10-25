
package dev.cyberdeck.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import dev.cyberdeck.ui.theme.*

@Composable
internal fun ConfigurationScreen(
    palette: DeckPalette,
    assignments: Map<String, String>,
    onAssignApp: (category: String, packageName: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var editingCategory by remember {
        mutableStateOf<String?>(null)
    }


    val installedApps = remember(context) {
        context.packageManager
            .getInstalledApplications(0)
            .mapNotNull { app ->
                val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(app.packageName)

                if (launchIntent != null) {
                    Triple(
                        app.loadLabel(context.packageManager).toString(),
                        app.packageName,
                        launchIntent
                    )
                } else {
                    null
                }
            }
            .sortedBy { it.first.lowercase() }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .border(1.dp, palette.border)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONFIGURATION",
                    color = palette.accent,
                    fontSize = 12.sp,
                    fontFamily = Mono,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "LOCAL SYSTEM SETTINGS",
                    color = palette.dimText,
                    fontSize = 7.sp,
                    fontFamily = Mono,
                    letterSpacing = 0.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .border(1.dp, palette.border)
                    .clickable(onClick = onBack)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HOME",
                    color = palette.accent,
                    fontSize = 9.sp,
                    fontFamily = Mono,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.border)
        )

        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.border)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "[ RAIL CONFIGURATION ]",
                color = palette.accent,
                fontSize = 9.sp,
                fontFamily = Mono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Configure the categories available on the system rail.",
                color = palette.dimText,
                fontSize = 9.sp,
                lineHeight = 13.sp
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(palette.border.copy(alpha = 0.5f))
            )

            if (editingCategory != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, palette.border)
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$editingCategory // SELECT APP",
                        color = palette.accent,
                        fontSize = 9.sp,
                        fontFamily = Mono,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "BACK",
                        modifier = Modifier.clickable {
                            editingCategory = null
                        }.fillMaxWidth(),
                        color = palette.dimText,
                        fontSize = 8.sp,
                        fontFamily = Mono
                    )

                    Text(
                      text = "DEFAULT",
                      color = palette.brightText,
                      modifier = Modifier
                          .fillMaxWidth()
                          .clickable {
                              val category = editingCategory
                              if (category != null) {
                                  onAssignApp(category, DEFAULT_RAIL_ASSIGNMENT)
                              }
                              editingCategory = null
                          }
                          .padding(vertical = 10.dp)
                    )

                    installedApps.forEach { (appName, packageName, _) ->
                        Text(
                            text = appName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                  val category = editingCategory
                                  if (category != null) {
                                      onAssignApp(category, packageName)
                                  }
                                  editingCategory = null
                                }
                                .padding(vertical = 8.dp),
                            color = palette.brightText,
                            fontSize = 9.sp
                        )
                    }
                }
            } else {
              RailCategories.forEachIndexed { index, category ->
                  Row(
                      modifier = Modifier
                          .fillMaxWidth()
                          .border(1.dp, palette.border)
                          .padding(horizontal = 8.dp, vertical = 10.dp),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      Text(
                          text = (index + 1).toString().padStart(2, '0'),
                          color = palette.dimText,
                          fontSize = 8.sp,
                          fontFamily = Mono
                      )

                      Column(
                          modifier = Modifier
                              .weight(1f)
                              .padding(start = 10.dp),
                          verticalArrangement = Arrangement.spacedBy(3.dp)
                      ) {
                          Text(
                              text = category,
                              color = palette.accent,
                              fontSize = 9.sp,
                              fontFamily = Mono,
                              fontWeight = FontWeight.Bold,
                              letterSpacing = 0.5.sp
                          )

                          assignments[category]?.let { packageName ->
                              Text(
                                  text = installedApps
                                      .firstOrNull { it.second == packageName }
                                      ?.first ?: packageName,
                                  color = palette.dimText,
                                  fontSize = 8.sp
                              )
                          }
                      }

                      Text(
                          text = "CHANGE",
                          modifier = Modifier.clickable {
                              editingCategory = category
                          },
                          color = palette.accent,
                          fontSize = 8.sp,
                          fontFamily = Mono,
                          fontWeight = FontWeight.Bold,
                          letterSpacing = 0.5.sp
                      )
                  }
              }
            }           
        }


        Spacer(Modifier.weight(1f))

        Text(
            text = "CYBERDECK // CONFIGURATION",
            color = palette.dimText,
            fontSize = 7.sp,
            fontFamily = Mono,
            letterSpacing = 0.5.sp
        )
    }
}
