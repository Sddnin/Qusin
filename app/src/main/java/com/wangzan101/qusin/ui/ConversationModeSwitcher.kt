package com.wangzan101.qusin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wangzan101.qusin.R
import com.wangzan101.qusin.ui.theme.QusinTheme
import com.wangzan101.qusin.ui.theme.QusinType

/** Two deeply rounded tabs with a clear light selected state, matching Personal and Work headers. */
@Composable
internal fun ConversationModeSwitcher(
    personal: Boolean,
    onWork: () -> Unit,
    onPersonal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(stringResource(R.string.mode_personal), stringResource(R.string.mode_work))
    val selected = if (personal) 0 else 1
    Row(
        modifier.fillMaxWidth()
            .height(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF171719))
            .border(1.dp, QusinTheme.colors.edge.copy(alpha = 0.82f), CircleShape)
            .padding(3.dp)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .clip(CircleShape)
                    .background(if (active) Color(0xFFEAEAEA) else Color.Transparent)
                    .selectable(active, role = Role.Tab) { if (index == 0) onPersonal() else onWork() }
                    .testTag(if (index == 0) "mode-personal" else "mode-work"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = QusinType.Caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = if (active) Color(0xFF171717) else Color(0xFF96969A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
