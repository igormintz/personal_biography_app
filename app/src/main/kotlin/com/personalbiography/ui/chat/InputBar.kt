package com.personalbiography.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InputBar(
    onSendText: (String) -> Unit,
    onStartRecord: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 200.dp),
                placeholder = { Text("הקלד/י הודעה או פקודה (/help)") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 6,
                colors =
                TextFieldDefaults.colors(
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
            if (text.isBlank()) {
                FilledIconButton(
                    onClick = onStartRecord,
                    modifier =
                    Modifier
                        .padding(start = 6.dp)
                        .size(48.dp),
                    colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "הקלט",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                FilledIconButton(
                    onClick = {
                        val toSend = text
                        text = ""
                        onSendText(toSend)
                    },
                    modifier =
                    Modifier
                        .padding(start = 6.dp)
                        .size(48.dp),
                    colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "שלח",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingOverlayBar(
    elapsedSec: Long,
    onCancel: () -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FilledIconButton(
                onClick = onCancel,
                colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("✕", color = MaterialTheme.colorScheme.onError)
            }
            val mm = elapsedSec / 60
            val ss = elapsedSec % 60
            Text(
                text = "● %d:%02d מקליט/ה…".format(mm, ss),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyLarge,
            )
            FilledIconButton(
                onClick = onSend,
                colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "שלח הקלטה",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
