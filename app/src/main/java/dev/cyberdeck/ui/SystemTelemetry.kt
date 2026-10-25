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
internal fun SystemTelemetry(palette: DeckPalette) {
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
           .fillMaxSize()
           .padding(vertical = 8.dp)
           .padding(horizontal = 8.dp)
           .padding(start = 15.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {


        Column(
            Modifier
            .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {

            // Battery
            TelemetryTile(
                palette = palette,
                label = "BATTERY",
                value = "$batteryLevel%",
                detail = if (charging) "CHARGING" else "ON BATTERY",
                active = true,
                modifier = Modifier
                    .fillMaxWidth()
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
            
            // Location
            TelemetryTile(
                palette = palette,
                label = "LOCATION",
                value = when {
                    !locationPermission -> "NO PERMISSION"
                    !locationEnabled -> "DISABLED"
                    latitude != null && longitude != null -> if(latitude != null && longitude != null){
                        "%.4f %.4f".format(
                            java.util.Locale.US,
                            latitude,
                            longitude
                        )
                    }else{
                        "ENABLED"
                    }
                    else -> "ACQUIRING..."
                },
                detail = if (latitude != null && longitude != null) {
                    "ENABLED"
                } else {
                    "GPS / NETWORK"
                },
                active = locationEnabled && latitude != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLocationTap() }
            )

            // Wi-Fi
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
                    .fillMaxWidth()
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
                active = phoneStatePermission ||
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

            // Bluetooth
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
                    .fillMaxWidth()
                    .clickable { onBluetoothTap() }
            )
        }

        // System Settings
        TelemetryTile(
            palette = palette,
            label = "SYSTEM STATE",
            value = "ALL SYSTEMS ONLINE",
            detail = "SYSTEM SETTINGS",
            active = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    openSettings(
                        android.provider.Settings.ACTION_SETTINGS
                    )
                },
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
