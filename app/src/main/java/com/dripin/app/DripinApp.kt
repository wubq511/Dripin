package com.dripin.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DripinApp() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = "Dripin bootstrap")
        }
    }
}
