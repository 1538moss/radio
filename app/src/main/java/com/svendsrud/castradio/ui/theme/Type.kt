package com.svendsrud.castradio.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

private val Defaults = Typography()

val CastRadioTypography = Typography(
    headlineMedium = Defaults.headlineMedium.copy(fontWeight = FontWeight.Black),
    titleMedium = Defaults.titleMedium.copy(fontWeight = FontWeight.Bold)
)
