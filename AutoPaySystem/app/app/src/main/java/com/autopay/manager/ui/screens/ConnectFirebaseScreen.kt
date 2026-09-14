package com.autopay.manager.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.autopay.manager.data.FirebaseConfigManager
import com.autopay.manager.ui.theme.TextSecondary

@Composable
fun ConnectFirebaseScreen(onConnected: () -> Unit) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        isLoading = true
        errorMessage = null
        try {
            val text = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: throw IllegalStateException("Could not read file")

            val config = FirebaseConfigManager.parse(text)
            if (config == null) {
                errorMessage = "This doesn't look like a valid google-services.json file."
            } else {
                FirebaseConfigManager.save(context, config)
                onConnected()
            }
        } catch (e: Exception) {
            errorMessage = "Couldn't read that file: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Connect your Firebase project", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        Text(
            "This app works with your own Firebase project. Upload the " +
                "google-services.json file you downloaded from the Firebase " +
                "console (Project settings → Your apps).",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { filePicker.launch("application/json") },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Text("Choose google-services.json")
            }
        }

        errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(it, modifier = Modifier.padding(14.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Don't have a Firebase project yet? Go to console.firebase.google.com, " +
                "create a project, add an Android app (any package name works), " +
                "enable Firestore Database, then download this same file from " +
                "Project settings.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
