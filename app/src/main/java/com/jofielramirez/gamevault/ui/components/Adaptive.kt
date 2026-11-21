package com.jofielramirez.gamevault.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Tablets, foldables and phones in landscape (Material "medium" width class and up). */
@Composable
fun isWideScreen(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

/**
 * Fills the screen but keeps the content at a readable width, centered, on large screens.
 * On phones it has no visible effect.
 */
fun Modifier.readableWidth(maxWidth: Dp = 840.dp): Modifier =
    this
        .fillMaxSize()
        .wrapContentWidth()
        .widthIn(max = maxWidth)
        .fillMaxWidth()
