package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.UsernameVibe
import com.example.ui.components.CopyFeedbackPreviewBox
import com.example.ui.components.VedographiContentCard
import com.example.ui.components.VedographiDefaults
import com.example.ui.components.rememberCopyPulseState

@Composable
fun UsernameScreen(
    usernameInput: String,
    selectedVibe: UsernameVibe,
    generatedUsernames: List<String>,
    onInputChange: (String) -> Unit,
    onSelectVibe: (UsernameVibe) -> Unit,
    onShuffle: () -> Unit,
    onCopyUsername: (String) -> Unit,
    onSaveUsername: (String) -> Unit,
    onShareUsername: (String) -> Unit,
    onCopyAllUsernames: ((List<String>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "uname_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Username Generator",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Standout handles for Instagram, BGMI & Telegram",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onShuffle,
                    shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                    colors = VedographiDefaults.primaryButtonColors(),
                    modifier = Modifier
                        .height(VedographiDefaults.ButtonHeight)
                        .testTag("username_mix_btn")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mix", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        item(key = "uname_input") {
            OutlinedTextField(
                value = usernameInput,
                onValueChange = onInputChange,
                label = { Text("Enter Name or Nickname") },
                placeholder = { Text("e.g. Vedo, Aarav, Riya") },
                singleLine = true,
                shape = RoundedCornerShape(VedographiDefaults.InputCornerRadius),
                colors = VedographiDefaults.textFieldColors(),
                trailingIcon = {
                    if (usernameInput.isNotEmpty()) {
                        IconButton(
                            onClick = { onInputChange("") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input_field")
            )
        }

        item(key = "uname_vibes") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(UsernameVibe.entries) { vibe ->
                    val selected = vibe == selectedVibe
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectVibe(vibe) },
                        label = { Text(vibe.label, style = MaterialTheme.typography.labelLarge) },
                        colors = VedographiDefaults.filterChipColors(),
                        border = VedographiDefaults.filterChipBorder(selected),
                        modifier = Modifier.height(42.dp)
                    )
                }
            }
        }

        if (onCopyAllUsernames != null && generatedUsernames.isNotEmpty()) {
            item(key = "uname_batch_bar") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${generatedUsernames.size} generated handles",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { onCopyAllUsernames(generatedUsernames) },
                        shape = RoundedCornerShape(VedographiDefaults.ButtonCornerRadius),
                        colors = VedographiDefaults.outlinedButtonColors(),
                        border = VedographiDefaults.outlinedButtonBorder(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("copy_all_usernames_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CopyAll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy All",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        items(generatedUsernames, key = { it }) { handle ->
            val copyPulse = rememberCopyPulseState()
            val triggerCopy = {
                copyPulse.trigger()
                onCopyUsername(handle)
            }
            VedographiContentCard(
                onClick = triggerCopy,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CopyFeedbackPreviewBox(
                        pulseState = copyPulse,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = handle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                    Row {
                        IconButton(
                            onClick = triggerCopy,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = "Copy username",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { onSaveUsername(handle) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                Icons.Filled.BookmarkAdd,
                                contentDescription = "Save username",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onShareUsername(handle) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share username",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
