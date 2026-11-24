package com.wangzan101.qusin.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wangzan101.qusin.R
import com.wangzan101.qusin.ui.components.SegmentedControl

/** The two existing conversations remain independent; this only changes the visible destination. */
@Composable
internal fun ConversationModeSwitcher(
    personal: Boolean,
    onWork: () -> Unit,
    onPersonal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedControl(
        options = listOf(stringResource(R.string.mode_personal), stringResource(R.string.mode_work)),
        selected = if (personal) 0 else 1,
        onSelect = { selected -> if (selected == 0) onPersonal() else onWork() },
        modifier = modifier,
        tags = listOf("mode-personal", "mode-work"),
    )
}
