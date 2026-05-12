package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.AssetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: Int,
    viewModel: AssetViewModel,
    onNavigateBack: () -> Unit
) {
    val issue by viewModel.getIssueById(issueId).collectAsState(initial = null)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        issue?.let { isLog ->
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                Text("Issue Type: ${isLog.issueType.name}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
                Text("Logged At: ${sdf.format(Date(isLog.loggedAt))}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Status: ${if (isLog.isResolved) "Resolved" else "Open"}")
                isLog.resolvedAt?.let {
                    Text("Resolved At: ${sdf.format(Date(it))}")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Description:", style = MaterialTheme.typography.titleSmall)
                Text(isLog.description)
                
                Spacer(modifier = Modifier.height(24.dp))
                if (!isLog.isResolved) {
                    Button(onClick = { viewModel.resolveIssue(isLog) }) {
                        Text("Mark Resolved")
                    }
                }
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

