package com.example.bplog.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// import com.example.bplog.BuildConfig

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("BPLog", style = MaterialTheme.typography.headlineSmall)
        Text("Version 1.0", style = MaterialTheme.typography.bodyMedium)
        Text("© 2026 TruliVerse", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text("Privacy friendly — no tracking.", style = MaterialTheme.typography.bodySmall)
    }
}

