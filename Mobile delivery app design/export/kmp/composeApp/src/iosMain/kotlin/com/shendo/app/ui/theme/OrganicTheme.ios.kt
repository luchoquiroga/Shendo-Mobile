package com.shendo.app.ui.theme

import androidx.compose.ui.text.font.FontFamily

// Register the two .ttf files in composeResources/font/ and load them with
// org.jetbrains.compose.resources.Font(Res.font.caprasimo_regular).
// Falling back to the system face keeps the module compiling before the
// font assets land.
actual fun organicHeadingFamily(): FontFamily = FontFamily.Default
actual fun organicBodyFamily(): FontFamily = FontFamily.Default
