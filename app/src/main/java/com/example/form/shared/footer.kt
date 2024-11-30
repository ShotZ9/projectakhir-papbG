package com.example.form.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Footer(modifier: Modifier = Modifier) {
    Text(
        text = "© 2024 Praktikum PAPB-G",
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}