package com.shendo.app

import androidx.compose.runtime.Composable
import com.shendo.app.ui.home.HomeScreen
import com.shendo.app.ui.theme.ShendoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ShendoTheme {
        HomeScreen()
    }
}
