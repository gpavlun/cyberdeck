
package dev.cyberdeck.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import dev.cyberdeck.ui.DeckPalette

private enum class AppScreen {
    DECK,
    TERMINAL,
    CONFIGURATION
}

@Composable
fun Main(
    onToggleSystemUi: () -> Unit,
    onOpenMediaAccessSettings: () -> Unit,
    homeResetKey: Int,
    callInProgress: Boolean
){
    val context = LocalContext.current

    var currentScreen by remember {
        mutableStateOf(AppScreen.DECK)
    }

    LaunchedEffect(homeResetKey) {
        currentScreen = AppScreen.DECK
    }

    // Shared palette selection.
    var selectedPaletteIndex by remember {
        mutableIntStateOf(3.coerceIn(0, DeckPalettes.lastIndex))
    }
    val palette = DeckPalettes[selectedPaletteIndex]

    // Shared terminal state.
    val terminalPreferences = remember(context) {
        context.getSharedPreferences(
            "cyberdeck_terminal",
            android.content.Context.MODE_PRIVATE
        )
    }

    var terminalHistory by remember(terminalPreferences) {
        mutableStateOf(loadTerminalHistory(terminalPreferences))
    }

    var terminalOutput by remember {
        mutableStateOf("LOCAL NODE READY")
    }

    fun recordTerminalEntry(entry: String) {
        if (entry.trim().equals("clear", ignoreCase = true)) {
            terminalHistory = emptyList()
        } else {
            terminalHistory = (terminalHistory + entry).takeLast(25)
        }

        saveTerminalHistory(terminalPreferences, terminalHistory)
        terminalOutput = entry
    }

    // Shared rail assignments used by both the deck and configuration.
    val railPreferences = remember(context) {
        context.getSharedPreferences(
            "cyberdeck_rail_assignments",
            android.content.Context.MODE_PRIVATE
        )
    }

    var railAssignments by remember(railPreferences) {
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

    BackHandler(enabled = currentScreen != AppScreen.DECK) {
        currentScreen = AppScreen.DECK
    }

    when (currentScreen) {
        AppScreen.DECK -> {
            CyberdeckScreen(
                onToggleSystemUi = onToggleSystemUi,
                onOpenMediaAccessSettings = onOpenMediaAccessSettings,
                selectedPaletteIndex = selectedPaletteIndex,
                onPaletteChange = { selectedPaletteIndex = it },
                terminalOutput = terminalOutput,
                onTerminalOutput = { terminalOutput = it },
                onOpenTerminal = {
                    currentScreen = AppScreen.TERMINAL
                },
                onOpenConfiguration = {
                    currentScreen = AppScreen.CONFIGURATION
                },
                callInProgress = callInProgress
            )
        }

        AppScreen.TERMINAL -> {
            TerminalScreen(
                palette = palette,
                terminalHistory = terminalHistory,
                onCommand = ::recordTerminalEntry,
                onCall = { target ->
                    // The terminal handles contact lookup and placing calls.
                    // This callback is retained for its current interface.
                    android.util.Log.d(
                        "Cyberdeck",
                        "Call requested for: $target"
                    )
                },
                onExit = {
                    currentScreen = AppScreen.DECK
                }
            )
        }

        AppScreen.CONFIGURATION -> {
            ConfigurationScreen(
                palette = palette,
                assignments = railAssignments,
                onAssignApp = assignRailApp,
                onBack = {
                    currentScreen = AppScreen.DECK
                }
            )
        }
    }
}
