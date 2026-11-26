package com.qusin.qusin.ui

import androidx.compose.runtime.Composable
import com.qusin.qusin.settings.McpServerStore

/** Routes the settings entry into the preset catalog, secure connect flow and server manager. */
@Composable
fun McpServersScreen(store: McpServerStore, onBack: () -> Unit) {
    com.qusin.qusin.mcp.ui.McpServersScreen(store, onBack)
}
