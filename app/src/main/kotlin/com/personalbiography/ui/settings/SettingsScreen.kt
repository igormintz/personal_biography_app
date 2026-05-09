package com.personalbiography.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val vm = rememberSettingsViewModel()
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("הגדרות") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ApiKeySection(state = state, onSave = vm::saveApiKey)
            Spacer(Modifier.height(16.dp))
            ModelSection(
                state = state,
                onChatModel = vm::setChatModel,
                onTranscribeModel = vm::setTranscribeModel,
                onLanguage = vm::setLanguage,
            )
            Spacer(Modifier.height(16.dp))
            UsageSection(state = state, onRefresh = vm::refreshUsage)
            Spacer(Modifier.height(16.dp))
            ExportSection(state = state, onExport = vm::exportAll)
            Spacer(Modifier.height(24.dp))
            Text(
                "האפליקציה שומרת את המפתח באופן מוצפן (Android Keystore + EncryptedSharedPreferences). הוא לא נשלח לשום מקום מלבד OpenAI.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ApiKeySection(
    state: SettingsUiState,
    onSave: (String) -> Unit,
) {
    var raw by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.elevatedCardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "מפתח OpenAI",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                if (state.hasApiKey) "מוגדר: ${state.apiKeyMasked}" else "טרם הוגדר.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = raw,
                onValueChange = { raw = it },
                placeholder = { Text("sk-...") },
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (visible) "Hide" else "Show",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (state.hasApiKey) {
                    OutlinedButton(onClick = { onSave("") }) { Text("מחק מפתח") }
                    Spacer(Modifier.height(0.dp))
                }
                Button(
                    enabled = raw.isNotBlank(),
                    onClick = {
                        onSave(raw)
                        raw = ""
                    },
                ) {
                    Text("שמור")
                }
            }
        }
    }
}

@Composable
private fun ModelSection(
    state: SettingsUiState,
    onChatModel: (String) -> Unit,
    onTranscribeModel: (String) -> Unit,
    onLanguage: (String) -> Unit,
) {
    var chat by remember(state.chatModel) { mutableStateOf(state.chatModel) }
    var tx by remember(state.transcribeModel) { mutableStateOf(state.transcribeModel) }
    var lang by remember(state.language) { mutableStateOf(state.language) }

    Card(colors = CardDefaults.elevatedCardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("מודלים", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = chat,
                onValueChange = {
                    chat = it
                    onChatModel(it)
                },
                label = { Text("מודל מבנה (chat)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = tx,
                onValueChange = {
                    tx = it
                    onTranscribeModel(it)
                },
                label = { Text("מודל תמלול") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = lang,
                onValueChange = {
                    lang = it
                    onLanguage(it)
                },
                label = { Text("שפת תמלול (he/en/...)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun UsageSection(
    state: SettingsUiState,
    onRefresh: () -> Unit,
) {
    Card(colors = CardDefaults.elevatedCardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("שימוש היום", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onRefresh) { Text("רענן") }
            }
            Spacer(Modifier.height(8.dp))
            val u = state.todayUsage
            Text("• אירועים: ${u.events}")
            Text("• שניות תמלול: ${"%.1f".format(u.transcribeSeconds)}")
            Text("• טוקנים LLM: ${u.tokensIn} ↓ / ${u.tokensOut} ↑")
            Text("• עלות מוערכת: $${"%.4f".format(u.costUsd.toDouble())}")
        }
    }
}

@Composable
private fun ExportSection(
    state: SettingsUiState,
    onExport: () -> Unit,
) {
    Card(colors = CardDefaults.elevatedCardColors()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ייצוא", style = MaterialTheme.typography.titleMedium)
            Text(
                "ייצא את כל הרישומים כ-JSON לתיקיית Documents של האפליקציה.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                Text("ייצא הכול")
            }
            state.lastExportPath?.let { path ->
                Spacer(Modifier.height(6.dp))
                Text(
                    "נשמר: $path",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.errorMessage?.let { err ->
                Spacer(Modifier.height(6.dp))
                Text(
                    "שגיאה: $err",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
