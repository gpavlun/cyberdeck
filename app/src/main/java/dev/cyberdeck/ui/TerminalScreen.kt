
package dev.cyberdeck.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

import org.json.JSONArray
import android.content.SharedPreferences

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import android.content.Intent
import android.net.Uri


@Composable
internal fun TerminalScreen(
    palette: DeckPalette,
    terminalHistory: List<String>,
    onCommand: (String) -> Unit,
    onCall: (String) -> Unit,
    onExit: () -> Unit
) {


    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<LaunchableApp>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        installedApps = kotlinx.coroutines.withContext(Dispatchers.IO) {
            loadLaunchableApps(context)
        }
    }

    var inputField by remember {
        mutableStateOf(TextFieldValue(""))
    }
    val input = inputField.text
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var callContacts by remember {
        mutableStateOf<List<CallContact>>(emptyList())
    }

    var contactsPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        contactsPermissionGranted = granted
        if (granted) {
            callContacts = loadCallContacts(context.contentResolver)
        }
    }


    LaunchedEffect(contactsPermissionGranted) {
        if (contactsPermissionGranted) {
            callContacts = loadCallContacts(context.contentResolver)
        }
    }

    var callPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        callPermissionGranted = granted
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(5.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, CutCornerShape(5.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = "CYBERDECK // 01",
                color = palette.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Mono
            )

            Text(
                text = "COMMAND LINE // LOCAL SESSION",
                color = palette.brightText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Mono
            )
        }

        // History area
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(CutCornerShape(5.dp))
                .background(Color.Black)
                .border(1.dp, palette.border, CutCornerShape(5.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            terminalHistory.forEach { entry ->
                Text(
                    text = entry,
                    color = palette.brightText,
                    fontSize = 10.sp,
                    fontFamily = Mono
                )
            }
        }

        // Suggestions
        val query = input.trim()

        val isCallMode = query.equals("call", ignoreCase = true) ||
            query.startsWith("call ", ignoreCase = true)

        val callQuery = if (isCallMode) {
            query.drop(4).trim()
        } else {
            ""
        }

        LaunchedEffect(isCallMode, contactsPermissionGranted) {
            if (isCallMode && !contactsPermissionGranted) {
                contactsPermissionLauncher.launch(
                    Manifest.permission.READ_CONTACTS
                )
            }
        }

        if (isCallMode) {
            val digits = callQuery.filter(Char::isDigit)

        val contactSuggestions = if (!contactsPermissionGranted) {
            emptyList()
        } else {
            callContacts
                .filter { contact ->
                    if (callQuery.isBlank()) {
                        true
                    } else {
                        contact.name.contains(callQuery, ignoreCase = true) ||
                            (
                                digits.isNotEmpty() &&
                                contact.phoneNumber
                                    .filter(Char::isDigit)
                                    .contains(digits)
                            )
                    }
                }
                .distinctBy { it.name to it.phoneNumber }
                .sortedWith(
                    compareBy<CallContact> {
                        if (callQuery.isBlank()) 0
                        else if (it.name.startsWith(callQuery, ignoreCase = true)) 0
                        else 1
                    }.thenBy { it.name.lowercase() }
                )
                .take(5)
        }

            if (contactSuggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    contactSuggestions.forEach { contact ->
                        Row(
                            modifier = Modifier
                                .clip(CutCornerShape(5.dp))
                                .background(palette.surface)
                                .border(
                                    1.dp,
                                    palette.border,
                                    CutCornerShape(5.dp)
                                )
                                .clickable {
                                    val command = "call ${contact.name}"
                                    onCommand(command)
                                    inputField = TextFieldValue("")
                                    onCall(contact.name)
                                    keyboardController?.show()
                                }
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = contact.name,
                                color = palette.brightText,
                                fontSize = 10.sp,
                                fontFamily = Mono,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        } else if (query.isNotEmpty()) {
            val appSuggestions = installedApps
                .filter { it.name.contains(query, ignoreCase = true) }
                .take(5)

            val commandSuggestions = terminalCommands
                .filter { it.name.startsWith(query, ignoreCase = true) }
                .take(5)

            val suggestions = commandSuggestions.map { it.name to true } +
                appSuggestions.map { it.name to false }

            if (suggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.forEach { (name, isCommand) ->
                        Row(
                            modifier = Modifier
                                .clip(CutCornerShape(5.dp))
                                .background(
                                    if (isCommand) palette.surface
                                    else Color.Black
                                )
                                .border(
                                    1.dp,
                                    palette.border,
                                    CutCornerShape(5.dp)
                                )
                                .clickable {
                                    if (isCommand) {
                                        val filledCommand = "$name "
                                        inputField = TextFieldValue(
                                            text = filledCommand,
                                            selection = TextRange(filledCommand.length)
                                        )
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    } else {
                                        val app = appSuggestions
                                            .firstOrNull { it.name == name }

                                        if (app != null && launchApp(context, app)) {
                                            onCommand("Launching ${app.name}")
                                            inputField = TextFieldValue("")
                                        }
                                    }
                                }
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                color = palette.brightText,
                                fontSize = 10.sp,
                                fontFamily = Mono,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }



        fun requestCallPermissionAndPlaceCall(phoneNumber: String) {
            if (phoneNumber.isBlank()) {
                onCommand("CALL ERROR: No phone number provided.")
                return
            }

            val hasCallPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED

            callPermissionGranted = hasCallPermission

            if (!hasCallPermission) {
                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                onCommand("CALL: Phone permission is not currently granted.")
                return
            }

            try {
                val intent = Intent(
                    Intent.ACTION_CALL,
                    android.net.Uri.parse("tel:${android.net.Uri.encode(phoneNumber, "+*#")}")
                )
                context.startActivity(intent)
            } catch (e: SecurityException) {
                callPermissionGranted = false
                onCommand("CALL ERROR: Phone permission was denied.")
            } catch (e: Exception) {
                onCommand("CALL ERROR: Could not start the call.")
            }
        }

        fun submitCommand() {
            val command = input.trim()
            if (command.isEmpty()) return

            val commandName = command.substringBefore(' ').lowercase()
            val argument = command.substringAfter(' ', "").trim()

            when (commandName) {
                "call" -> {
                    if (argument.isBlank()) {
                        onCommand(command)
                        inputField = TextFieldValue("")
                        return
                    }

                    onCommand(command)
                    inputField = TextFieldValue("")

                    val phoneNumber = argument
                        .filter { it.isDigit() || it == '+' || it == '*' || it == '#' }

                    val matchingContacts = callContacts.filter { contact ->
                        contact.name.equals(argument, ignoreCase = true)
                    }

                    when {
                        matchingContacts.size == 1 -> {
                            requestCallPermissionAndPlaceCall(
                                matchingContacts.first().phoneNumber
                            )
                        }

                        matchingContacts.size > 1 -> {
                            onCommand("Multiple contacts named '$argument'. Use a more specific name.")
                        }

                        phoneNumber.isNotBlank() -> {
                            requestCallPermissionAndPlaceCall(phoneNumber)
                        }

                        else -> {
                            onCommand("No exact contact match for '$argument'.")
                        }
                    }
                }

                else -> {
                    onCommand(command)
                    inputField = TextFieldValue("")
                }
            }

            keyboardController?.show()
        }


        


        // Input row 
        Row(
            modifier = Modifier
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
                value = inputField,
                onValueChange = { inputField = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = TextStyle(
                    color = palette.brightText,
                    fontSize = 10.sp,
                    fontFamily = Mono
                ),
                cursorBrush = SolidColor(palette.accent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { submitCommand() }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (input.isEmpty()) {
                            Text(
                                text = "enter command...",
                                color = palette.dimText,
                                fontSize = 10.sp,
                                fontFamily = Mono
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Text(
                "↵",
                modifier = Modifier
                    .clickable {
                        submitCommand()
                    }
                    .padding(start = 8.dp),
                color = palette.accent,
                fontSize = 13.sp
            )
        }


    }
}



private const val MAX_TERMINAL_HISTORY = 25
private const val TERMINAL_HISTORY_KEY = "terminal_history"

internal fun loadTerminalHistory(
    prefs: SharedPreferences
): List<String> {
    val json = prefs.getString(TERMINAL_HISTORY_KEY, null)
        ?: return emptyList()

    return try {
        val array = JSONArray(json)
        (0 until array.length())
            .map { array.getString(it) }
            .takeLast(MAX_TERMINAL_HISTORY)
    } catch (_: Exception) {
        emptyList()
    }
}

internal fun saveTerminalHistory(
    prefs: SharedPreferences,
    history: List<String>
) {
    val array = JSONArray()
    history.takeLast(MAX_TERMINAL_HISTORY).forEach(array::put)

    prefs.edit()
        .putString(TERMINAL_HISTORY_KEY, array.toString())
        .apply()
}

internal data class TerminalCommand(
    val name: String,
    val description: String
)

internal val terminalCommands = listOf(
    TerminalCommand("call", "Call a contact or number"),
    TerminalCommand("clear", "Clear terminal history"),
    TerminalCommand("sms", "Text a contact or number")
)