package com.example.dripin4.ui.features.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.designsystem.components.GlassCard
import com.example.dripin4.ui.designsystem.components.GlassCardTone
import com.example.dripin4.ui.designsystem.components.GlassInfoPill
import com.example.dripin4.ui.designsystem.components.GlassSectionHeading

@Composable
fun EmptyStateCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier,
        tone = GlassCardTone.Neutral,
        contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
    ) {
        GlassInfoPill(
            text = "Empty",
            tint = GlassPalette.AccentMint,
        )
        Spacer(modifier = Modifier.height(DripSpacing.Small))
        GlassSectionHeading(title = title, body = body)
    }
}

@Composable
fun NoResultStateCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.testTag("state_no_result"),
        tone = GlassCardTone.Neutral,
        contentPadding = PaddingValues(horizontal = DripSpacing.CardPadding, vertical = DripSpacing.Medium),
    ) {
        GlassInfoPill(
            text = "No match",
            tint = GlassPalette.AccentMint,
        )
        Spacer(modifier = Modifier.height(DripSpacing.Small))
        GlassSectionHeading(title = title, body = body)
    }
}
