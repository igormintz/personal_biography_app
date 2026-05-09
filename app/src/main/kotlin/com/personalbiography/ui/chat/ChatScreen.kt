package com.personalbiography.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.personalbiography.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onOpenSettings: () -> Unit) {
    val vm = rememberChatViewModel()
    val messages by vm.messages.collectAsState()
    val recordingState by vm.recordingState.collectAsState()
    val pending by vm.pendingPrompt.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val recordPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                vm.startRecording()
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("חסרה הרשאת מיקרופון")
                }
            }
        }

    fun maybeStartRecording() {
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) {
            vm.startRecording()
        } else {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        bottomBar = {
            Column(
                modifier =
                Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
            ) {
                if (pending != null) {
                    PendingHint(pending = pending!!, onCancel = vm::cancelPending)
                }
                when (val state = recordingState) {
                    ChatViewModel.RecordingState.Idle ->
                        InputBar(
                            onSendText = vm::sendText,
                            onStartRecord = ::maybeStartRecording,
                        )
                    is ChatViewModel.RecordingState.Recording ->
                        RecordingPanel(
                            startedAtMs = state.startedAtMs,
                            onCancel = vm::cancelRecording,
                            onSend = vm::stopAndSendRecording,
                        )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (messages.isEmpty()) {
                EmptyChatHint()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message,
                            onResultAction = { action ->
                                when (action) {
                                    is ChatBubbleAction.EditTranscript ->
                                        vm.sendText("/edit ${action.entry.shortId}")
                                    is ChatBubbleAction.Restructure ->
                                        vm.runRestructure(action.entry.shortId)
                                    is ChatBubbleAction.OverrideTags ->
                                        vm.sendText("/tags ${action.entry.shortId}")
                                    is ChatBubbleAction.CopyJson -> {
                                        clipboard.setText(AnnotatedString(vm.copyEntryAsJson(action.entry)))
                                        scope.launch {
                                            snackbarHostState.showSnackbar("הועתק ל-clipboard")
                                        }
                                    }
                                    is ChatBubbleAction.Delete -> vm.deleteEntry(action.entry)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatHint() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text =
            "שלום! זוהי הביוגרפיה האישית שלך.\n" +
                "לחץ/י על הכפתור הכחול כדי להקליט,\n" +
                "או הקלד/י טקסט / פקודה (/help).",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PendingHint(
    pending: ChatViewModel.Pending,
    onCancel: () -> Unit,
) {
    val text =
        when (pending) {
            is ChatViewModel.Pending.Edit -> "ההודעה הבאה תיחשב לתמליל מחודש לרישום ${pending.shortId}."
            is ChatViewModel.Pending.Tags ->
                "ההודעה הבאה תיחשב לרשימת תגיות (מופרדות בפסיקים) לרישום ${pending.shortId}."
        }
    androidx.compose.material3.Surface(
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodySmall,
            )
            androidx.compose.material3.TextButton(onClick = onCancel) {
                Text("ביטול")
            }
        }
    }
}

@Composable
private fun RecordingPanel(
    startedAtMs: Long,
    onCancel: () -> Unit,
    onSend: () -> Unit,
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAtMs) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(250L)
        }
    }
    val elapsedSec = ((nowMs - startedAtMs) / 1000L).coerceAtLeast(0L)
    RecordingOverlayBar(
        elapsedSec = elapsedSec,
        onCancel = onCancel,
        onSend = onSend,
    )
}
