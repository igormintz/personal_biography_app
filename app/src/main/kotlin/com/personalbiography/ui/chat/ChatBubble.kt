package com.personalbiography.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personalbiography.data.db.EntryEntity
import com.personalbiography.domain.Replies

private val MAX_BUBBLE_WIDTH = 320.dp

@Composable
fun ChatBubble(
    message: ChatMessage,
    onResultAction: (ChatBubbleAction) -> Unit,
) {
    when (message) {
        is ChatMessage.UserText -> UserTextBubble(message.body)
        is ChatMessage.UserVoice -> UserVoiceBubble(message.durationSec)
        is ChatMessage.BotStatus -> BotPlainBubble(message.text, isStatus = true)
        is ChatMessage.BotInfo -> BotPlainBubble(message.text, isStatus = false)
        is ChatMessage.BotError -> BotPlainBubble(message.text, isStatus = false, isError = true)
        is ChatMessage.BotResult -> BotResultBubble(message.entry, onResultAction)
    }
}

@Composable
private fun UserTextBubble(body: String) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier =
            Modifier
                .widthIn(max = MAX_BUBBLE_WIDTH)
                .clip(RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun UserVoiceBubble(durationSec: Double) {
    val mm = (durationSec / 60).toInt()
    val ss = (durationSec % 60).toInt()
    val label = "🎤 %d:%02d".format(mm, ss)
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Row(
            modifier =
            Modifier
                .widthIn(max = MAX_BUBBLE_WIDTH)
                .clip(RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun BotPlainBubble(
    text: String,
    isStatus: Boolean,
    isError: Boolean = false,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        val bg =
            when {
                isError -> MaterialTheme.colorScheme.errorContainer
                isStatus -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        val fg =
            when {
                isError -> MaterialTheme.colorScheme.onErrorContainer
                isStatus -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            }
        Box(
            modifier =
            Modifier
                .widthIn(max = MAX_BUBBLE_WIDTH)
                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(text = text, color = fg, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BotResultBubble(
    entry: EntryEntity,
    onAction: (ChatBubbleAction) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier =
            Modifier
                .widthIn(max = MAX_BUBBLE_WIDTH)
                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = { /* tap-to-expand later */ },
                    onLongClick = { menuExpanded = true },
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column {
                if (!entry.transcript.isNullOrBlank()) {
                    Text(
                        text = "📝 ${entry.transcript}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (!entry.summary.isNullOrBlank()) {
                    Text(
                        text = "📌 ${entry.summary}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp),
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (entry.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                    ) {
                        entry.tags.forEach { tag ->
                            AssistChip(
                                onClick = { /* future: filter by tag */ },
                                label = { Text(tag) },
                                modifier = Modifier.padding(end = 4.dp),
                                colors =
                                AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            )
                        }
                    }
                }
                if (entry.followUpQuestions.isNotEmpty()) {
                    Text(
                        text = "❓ שאלות המשך:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    entry.followUpQuestions.forEachIndexed { index, q ->
                        Text(
                            text = "${index + 1}. $q",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Text(
                    text = "🆔 ${entry.shortId}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth().wrapContentWidth(Alignment.End),
                )

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("עריכת תמליל (/edit)") },
                        onClick = {
                            menuExpanded = false
                            onAction(ChatBubbleAction.EditTranscript(entry))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("הרצה חוזרת (/restructure)") },
                        onClick = {
                            menuExpanded = false
                            onAction(ChatBubbleAction.Restructure(entry))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("דריסת תגיות (/tags)") },
                        onClick = {
                            menuExpanded = false
                            onAction(ChatBubbleAction.OverrideTags(entry))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("העתק טקסט מלא") },
                        onClick = {
                            menuExpanded = false
                            clipboard.setText(AnnotatedString(Replies.formatFullBundle(entry.toView())))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("העתק כ-JSON") },
                        onClick = {
                            menuExpanded = false
                            onAction(ChatBubbleAction.CopyJson(entry))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("מחק") },
                        onClick = {
                            menuExpanded = false
                            onAction(ChatBubbleAction.Delete(entry))
                        },
                    )
                }
            }
        }
    }
}

sealed interface ChatBubbleAction {
    data class EditTranscript(val entry: EntryEntity) : ChatBubbleAction

    data class Restructure(val entry: EntryEntity) : ChatBubbleAction

    data class OverrideTags(val entry: EntryEntity) : ChatBubbleAction

    data class CopyJson(val entry: EntryEntity) : ChatBubbleAction

    data class Delete(val entry: EntryEntity) : ChatBubbleAction
}
