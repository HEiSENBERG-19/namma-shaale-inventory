// AssetDetailScreen.kt
package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.data.AssetEntity
import com.example.myapplication.data.Condition
import com.example.myapplication.data.ConditionHistoryEntity
import com.example.myapplication.data.IssueLogEntity
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AssetViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    assetId: Int,
    viewModel: AssetViewModel,
    onBack: () -> Unit
) {
    var asset by remember { mutableStateOf<AssetEntity?>(null) }
    var history by remember { mutableStateOf<List<ConditionHistoryEntity>>(emptyList()) }
    var issues by remember { mutableStateOf<List<IssueLogEntity>>(emptyList()) }
    var showConditionUpdateDialog by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            asset?.let { viewModel.updateAssetPhoto(it, tempUri.toString()) }
        }
    }

    LaunchedEffect(assetId) {
        viewModel.getAssetById(assetId).collect { asset = it }
    }
    LaunchedEffect(assetId) {
        viewModel.getConditionHistoryForAsset(assetId).collect { history = it }
    }

    Scaffold(
        containerColor = SystemBackground,
        topBar = {
            TopAppBar(
                title = { Text(asset?.name ?: "Loading...", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SystemBackground)
            )
        }
    ) { padding ->
        if (asset == null) return@Scaffold
        val currentAsset = asset!!

        if (showConditionUpdateDialog) {
            HealthCheckDialog(
                asset = currentAsset,
                onDismiss = { showConditionUpdateDialog = false },
                onSave = { condition, note ->
                    viewModel.updateCondition(currentAsset, condition, note, null, "Current Teacher")
                    showConditionUpdateDialog = false
                }
            )
        }

        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp)) {

            // Hero Photo Area
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp).padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated),
                    onClick = {
                        val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        tempUri = uri
                        cameraLauncher.launch(uri)
                    }
                ) {
                    if (currentAsset.photoUri != null) {
                        AsyncImage(
                            model = currentAsset.photoUri,
                            contentDescription = "Asset Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextTertiary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to add photo", color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        val conditionColor = when (currentAsset.currentCondition) {
                            Condition.GREEN -> StatusGreen
                            Condition.YELLOW -> StatusYellow
                            Condition.RED -> StatusRed
                        }

                        // Status Badge
                        Surface(color = conditionColor.copy(alpha = 0.15f), shape = CircleShape) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(conditionColor, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(currentAsset.currentCondition.name, color = conditionColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        DetailRow("Category", currentAsset.category.name)
                        DetailRow("Serial Number", currentAsset.serialNumber ?: "N/A")
                        DetailRow("Acquired On", currentAsset.acquisitionDate)
                        DetailRow("Purchase Value", currentAsset.purchaseValue?.toString() ?: "N/A")
                    }
                }
            }

            // Action Buttons
            item {
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showConditionUpdateDialog = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Update Status", fontWeight = FontWeight.Bold) }

                    if (currentAsset.isActive) {
                        Button(
                            onClick = {
                                viewModel.markDisposed(currentAsset)
                                onBack()
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed.copy(alpha = 0.15f), contentColor = StatusRed),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Mark Disposed", fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // History Section
            item {
                Spacer(Modifier.height(32.dp))
                Text("Condition History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(16.dp))
            }

            items(history) { record ->
                val color = when(record.condition) { Condition.GREEN -> StatusGreen; Condition.YELLOW -> StatusYellow; Condition.RED -> StatusRed }
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Box(modifier = Modifier.padding(top = 6.dp).size(10.dp).background(color, CircleShape))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(record.condition.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Recorded by ${record.recordedBy}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        if (!record.note.isNullOrBlank()) {
                            Text("Note: ${record.note}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextTertiary)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}