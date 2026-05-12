package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import com.example.myapplication.data.AssetEntity
import com.example.myapplication.data.Condition
import com.example.myapplication.data.IssueType
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AssetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(
    viewModel: AssetViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onBack: () -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    var selectedAsset by remember { mutableStateOf<AssetEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDisposed by remember { mutableStateOf(false) }

    val filteredAssets = assets.filter {
        (if (showDisposed) !it.isActive else it.isActive) &&
                (it.name.contains(searchQuery, ignoreCase = true) ||
                        (it.serialNumber?.contains(searchQuery, ignoreCase = true) ?: false))
    }

    if (selectedAsset != null) {
        HealthCheckDialog(
            asset = selectedAsset!!,
            onDismiss = { selectedAsset = null },
            onSave = { condition, note ->
                viewModel.updateCondition(selectedAsset!!, condition, note, null, "Teacher")
                if (condition != Condition.GREEN && !note.isNullOrBlank()) {
                    viewModel.logIssue(selectedAsset!!.id, IssueType.OTHER, note ?: "", null)
                }
                selectedAsset = null
            }
        )
    }

    Scaffold(
        containerColor = SystemBackground,
        topBar = {
            TopAppBar(
                title = { Text("ಆಸ್ತಿ ನೋಂದಣಿ (Assets)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SystemBackground),
                actions = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Show Disposed", modifier = Modifier.padding(end = 8.dp), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Switch(
                            checked = showDisposed,
                            onCheckedChange = { showDisposed = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue, checkedTrackColor = AccentBlue.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Asset")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp)) {

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or serial...", color = TextTertiary) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CardBackgroundElevated,
                    focusedContainerColor = CardBackgroundElevated,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AccentBlue
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredAssets) { asset ->
                    AssetListItem(asset = asset, onClick = { onNavigateToDetail(asset.id) })
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun AssetListItem(asset: AssetEntity, onClick: () -> Unit) {
    val conditionColor = when (asset.currentCondition) {
        Condition.GREEN -> StatusGreen
        Condition.YELLOW -> StatusYellow
        Condition.RED -> StatusRed
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(12.dp).background(conditionColor, CircleShape))
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${asset.category.name} • S/N: ${asset.serialNumber ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Details", tint = TextTertiary)
        }
    }
}

@Composable
fun HealthCheckDialog(
    asset: AssetEntity,
    onDismiss: () -> Unit,
    onSave: (Condition, String?) -> Unit
) {
    var condition by remember { mutableStateOf(asset.currentCondition) }
    var issueLog by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Update Status: ${asset.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(Condition.GREEN to "Working (Green)", Condition.YELLOW to "Needs Repair (Yellow)", Condition.RED to "Broken (Red)").forEach { (cond, label) ->
                    val color = when(cond) { Condition.GREEN -> StatusGreen; Condition.YELLOW -> StatusYellow; Condition.RED -> StatusRed }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { condition = cond },
                        colors = CardDefaults.cardColors(containerColor = if (condition == cond) color.copy(alpha = 0.15f) else CardBackgroundElevated),
                        border = BorderStroke(1.dp, if (condition == cond) color else Color.Transparent)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, color = if (condition == cond) color else TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (condition != Condition.GREEN) {
                    OutlinedTextField(
                        value = issueLog,
                        onValueChange = { issueLog = it },
                        placeholder = { Text("Describe the issue...") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CardBackgroundElevated,
                            focusedContainerColor = CardBackgroundElevated,
                            focusedBorderColor = AccentBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(condition, if (condition == Condition.GREEN) null else issueLog) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Save Status", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) { Text("Cancel") }
        }
    )
}