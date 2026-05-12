package com.example.myapplication.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Condition
import com.example.myapplication.data.AssetEntity
import com.example.myapplication.data.IssueLogEntity
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AssetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryReportScreen(viewModel: AssetViewModel) {
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val allAssets by viewModel.allAssets.collectAsState()
    val openIssues by viewModel.openIssues.collectAsState()
    val context = LocalContext.current
    val schoolName = schoolProfile?.schoolName ?: "Unknown School"

    Scaffold(
        containerColor = SystemBackground,
        topBar = {
            TopAppBar(
                title = { Text("ವರದಿ (Summary Report)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SystemBackground),
                actions = {
                    IconButton(onClick = {
                        val reportContent = buildSummaryReportContent(allAssets, openIssues)
                        shareReportText(context, reportContent)
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Report", tint = AccentBlue)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize()) {

            // Document Header
            item {
                Spacer(Modifier.height(16.dp))
                Text(schoolName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                Text("Generated: ${sdf.format(Date())}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.height(32.dp))
            }

            // Asset Breakdown Cards
            item {
                val green = allAssets.count { it.currentCondition == Condition.GREEN }
                val yellow = allAssets.count { it.currentCondition == Condition.YELLOW }
                val red = allAssets.count { it.currentCondition == Condition.RED }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total Inventory", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        Text("${allAssets.size} Assets", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextPrimary)

                        Spacer(modifier = Modifier.height(24.dp))

                        ReportStatusRow("Working (GREEN)", green, StatusGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        ReportStatusRow("Needs Repair (YELLOW)", yellow, StatusYellow)
                        Spacer(modifier = Modifier.height(12.dp))
                        ReportStatusRow("Broken/Disposed (RED)", red, StatusRed)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatusRow(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
        Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

fun buildSummaryReportContent(assets: List<AssetEntity>, issues: List<IssueLogEntity>): String {
    // Keeping your original logic here!
    val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date())
    val green = assets.count { it.currentCondition == Condition.GREEN }
    val yellow = assets.count { it.currentCondition == Condition.YELLOW }
    val red = assets.count { it.currentCondition == Condition.RED }

    val categoryBreakdown = assets.groupBy { it.category }
        .map { "• ${it.key.name}: ${it.value.size}" }
        .joinToString("\n")

    return """
        Namma Shaale Inventory Report — $date
        Total Assets: ${assets.size}
        ----------------------------------
        Working (Green): $green
        Needs Repair (Yellow): $yellow
        Broken (Red): $red
        
        Open Issues: ${issues.size}
        
        Category Breakdown:
        $categoryBreakdown
    """.trimIndent()
}

fun shareReportText(context: Context, content: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Namma Shaale Inventory Report")
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(intent, "Share Report"))
}