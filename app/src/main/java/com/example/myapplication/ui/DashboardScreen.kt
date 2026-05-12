package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Category
import com.example.myapplication.data.Condition
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AssetViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AssetViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToHealthCheck: () -> Unit,
    onNavigateToIssues: () -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val schoolName = schoolProfile?.schoolName ?: "GHPS Malleshwaram"
    val currentDate = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date())

    val workingCount = assets.count { it.currentCondition == Condition.GREEN }
    val repairCount = assets.count { it.currentCondition == Condition.YELLOW }
    val attentionCount = assets.count { it.currentCondition == Condition.RED }

    Scaffold(
        containerColor = SystemBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Asset")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GOVERNMENT SCHOOL",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = schoolName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2x2 Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard(
                        title = "Total Assets", count = assets.size,
                        bgColor = CardBlueBg, textColor = CardBlueText, icon = Icons.Rounded.List,
                        modifier = Modifier.weight(1f), onClick = onNavigateToList
                    )
                    SummaryCard(
                        title = "Working", count = workingCount,
                        bgColor = CardGreenBg, textColor = CardGreenText, icon = Icons.Rounded.CheckCircle,
                        modifier = Modifier.weight(1f), onClick = onNavigateToList
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard(
                        title = "Attention", count = attentionCount,
                        bgColor = CardYellowBg, textColor = CardYellowText, icon = Icons.Rounded.Warning,
                        modifier = Modifier.weight(1f), onClick = onNavigateToIssues
                    )
                    SummaryCard(
                        title = "Repair", count = repairCount,
                        bgColor = CardRedBg, textColor = CardRedText, icon = Icons.Rounded.Info,
                        modifier = Modifier.weight(1f), onClick = onNavigateToReport
                    )
                }
            }

            // Asset Health by Category Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Asset Health by Category",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // We render only categories that actually have assets
                        val categoriesWithAssets = Category.entries.filter { cat -> assets.any { it.category == cat } }

                        if (categoriesWithAssets.isEmpty()) {
                            Text("No assets added yet.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            categoriesWithAssets.forEach { category ->
                                val catAssets = assets.filter { it.category == category }
                                val green = catAssets.count { it.currentCondition == Condition.GREEN }
                                val yellow = catAssets.count { it.currentCondition == Condition.YELLOW }
                                val red = catAssets.count { it.currentCondition == Condition.RED }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(0.4f)
                                    )
                                    SegmentedProgressBar(
                                        greenCount = green,
                                        orangeCount = yellow,
                                        redCount = red,
                                        modifier = Modifier.weight(0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }

            // --- TEMPORARY DEVELOPER TOOL ---
            item {
                OutlinedButton(
                    onClick = { viewModel.insertSampleData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                ) {
                    Icon(Icons.Rounded.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DEV: Load Sample Data")
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}