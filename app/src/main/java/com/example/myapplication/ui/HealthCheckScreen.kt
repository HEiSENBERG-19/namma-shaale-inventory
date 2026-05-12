package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AssetEntity
import com.example.myapplication.data.Condition
import com.example.myapplication.viewmodel.AssetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCheckScreen(
    viewModel: AssetViewModel,
    onBack: () -> Unit
) {
    val allAssets by viewModel.allAssets.collectAsState()
    val activeTeacher by viewModel.activeTeacher.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var sessionId by remember { mutableStateOf<Long?>(null) }
    var startedAt by remember { mutableStateOf(0L) }
    var currentIndex by remember { mutableStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }
    var itemsReviewed by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }

    val teacherName = activeTeacher ?: "Unknown Teacher"

    val moveToNext = {
        itemsReviewed++
        currentIndex++
        note = ""
        if (currentIndex >= allAssets.size && sessionId != null) {
            viewModel.completeHealthCheckSession(
                sessionId = sessionId!!.toInt(),
                conductedBy = teacherName,
                itemsReviewed = itemsReviewed,
                totalItems = allAssets.size,
                startedAt = startedAt
            )
            isCompleted = true
        }
    }

    LaunchedEffect(allAssets) {
        if (allAssets.isNotEmpty() && sessionId == null) {
            val totalItems = allAssets.size
            startedAt = System.currentTimeMillis()
            coroutineScope.launch {
                val id = viewModel.startHealthCheckSession(totalItems, teacherName)
                sessionId = id
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Monthly Health Check") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (allAssets.isEmpty()) {
                Text("No active assets to check.")
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Return to Dashboard")
                }
                return@Scaffold
            }

            if (isCompleted) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Session Complete!", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                        Text("Items Reviewed: $itemsReviewed / ${allAssets.size}")
                        val diff = System.currentTimeMillis() - startedAt
                        val seconds = (diff / 1000) % 60
                        val minutes = (diff / (1000 * 60)) % 60
                        Text("Duration: ${minutes}m ${seconds}s")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text("Done")
                        }
                    }
                }
                return@Scaffold
            }

            val total = allAssets.size
            LinearProgressIndicator(
                progress = currentIndex.toFloat() / total,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Progress: $currentIndex / $total", modifier = Modifier.padding(vertical = 8.dp))

            if (currentIndex < allAssets.size) {
                val currentAsset = allAssets[currentIndex]

                Card(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentAsset.name, style = MaterialTheme.typography.headlineMedium)
                        Text("Category: ${currentAsset.category.name}")
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Last Known Condition: ${currentAsset.currentCondition.name}", color = Color.Gray)
                        
                        Spacer(Modifier.weight(1f))
                        
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Optional Note") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))

                        Text("Select Current Condition:", style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = {
                                    viewModel.updateCondition(currentAsset, Condition.GREEN, note.ifBlank { null }, null, teacherName)
                                    moveToNext()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) { Text("Green") }
                            Button(
                                onClick = {
                                    viewModel.updateCondition(currentAsset, Condition.YELLOW, note.ifBlank { null }, null, teacherName)
                                    moveToNext()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17))
                            ) { Text("Yellow") }
                            Button(
                                onClick = {
                                    viewModel.updateCondition(currentAsset, Condition.RED, note.ifBlank { null }, null, teacherName)
                                    moveToNext()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                            ) { Text("Red") }
                        }
                    }
                }
            }
        }
    }

}





