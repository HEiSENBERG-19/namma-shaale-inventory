package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AssetEntity
import com.example.myapplication.data.IssueLogEntity
import com.example.myapplication.data.IssueType
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AssetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueLogScreen(
    viewModel: AssetViewModel,
    navigateToIssueDetail: (Int) -> Unit
) {
    val allIssues by viewModel.allIssues.collectAsState()
    val allAssets by viewModel.allAssets.collectAsState()

    var filterState by remember { mutableStateOf("All") }
    var showLogDialog by remember { mutableStateOf(false) }

    val filteredIssues = remember(allIssues, filterState) {
        when (filterState) {
            "Open" -> allIssues.filter { !it.isResolved }
            "Resolved" -> allIssues.filter { it.isResolved }
            else -> allIssues
        }
    }

    Scaffold(
        containerColor = SystemBackground,
        topBar = {
            TopAppBar(
                title = { Text("ಸಮಸ್ಯೆಗಳು (Issues)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SystemBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLogDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Log Issue")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp)) {

            // Modern Filter Chips
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Open", "Resolved").forEach { option ->
                    val isSelected = filterState == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterState = option },
                        label = { Text(option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue.copy(alpha = 0.15f),
                            selectedLabelColor = AccentBlue,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = CardBorder,
                            selectedBorderColor = AccentBlue,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = CircleShape
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredIssues) { issue ->
                    val asset = allAssets.find { it.id == issue.assetId }
                    IssueRow(
                        issue = issue,
                        assetName = asset?.name ?: "Unknown",
                        onClick = { navigateToIssueDetail(issue.issueId) }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

    if (showLogDialog) {
        LogIssueDialog(
            assets = allAssets,
            onDismiss = { showLogDialog = false },
            onSubmit = { assetId, type, desc, photo ->
                viewModel.logIssue(assetId, type, desc, photo)
                showLogDialog = false
            }
        )
    }
}

@Composable
fun IssueRow(issue: IssueLogEntity, assetName: String, onClick: () -> Unit) {
    val statusColor = if (issue.isResolved) StatusGreen else StatusRed

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = assetName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

                // Status Badge
                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if (issue.isResolved) "Resolved" else "Open",
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = CardBackgroundElevated, shape = RoundedCornerShape(6.dp)) {
                    Text(issue.issueType.name, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                Text(text = sdf.format(Date(issue.loggedAt)), style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogIssueDialog(
    assets: List<AssetEntity>,
    onDismiss: () -> Unit,
    onSubmit: (Int, IssueType, String, String?) -> Unit
) {
    var selectedAssetId by remember { mutableStateOf<Int?>(null) }
    var selectedType by remember { mutableStateOf(IssueType.OTHER) }
    var description by remember { mutableStateOf("") }
    var assetExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        titleContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Log New Issue", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = assetExpanded, onExpandedChange = { assetExpanded = it }) {
                    OutlinedTextField(
                        value = assets.find { it.id == selectedAssetId }?.name ?: "Select Asset",
                        onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, unfocusedContainerColor = CardBackgroundElevated, focusedContainerColor = CardBackgroundElevated)
                    )
                    ExposedDropdownMenu(expanded = assetExpanded, onDismissRequest = { assetExpanded = false }) {
                        assets.forEach { asset ->
                            DropdownMenuItem(text = { Text(asset.name) }, onClick = { selectedAssetId = asset.id; assetExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedType.name, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, unfocusedContainerColor = CardBackgroundElevated, focusedContainerColor = CardBackgroundElevated)
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        IssueType.values().forEach { t ->
                            DropdownMenuItem(text = { Text(t.name) }, onClick = { selectedType = t; typeExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it }, label = { Text("Description", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, unfocusedContainerColor = CardBackgroundElevated, focusedContainerColor = CardBackgroundElevated)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedAssetId?.let { onSubmit(it, selectedType, description, null) } },
                enabled = selectedAssetId != null && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) { Text("Submit", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) { Text("Cancel") }
        }
    )
}