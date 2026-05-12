package com.example.myapplication.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.myapplication.data.AssetEntity
import com.example.myapplication.data.Condition
import com.example.myapplication.viewmodel.AssetViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairRequestScreen(
    viewModel: AssetViewModel,
    onNavigateBack: () -> Unit
) {
    val allAssets by viewModel.allAssets.collectAsState()
    val openIssues by viewModel.openIssues.collectAsState()
    val schoolProfile by viewModel.schoolProfile.collectAsState()
    val context = LocalContext.current

    // RED assets + assets with open issues
    val defaultAssets = remember(allAssets, openIssues) {
        val issueAssetIds = openIssues.map { it.assetId }.toSet()
        allAssets.filter { it.currentCondition == Condition.RED || it.id in issueAssetIds }
    }

    var selectedAssets by remember(defaultAssets) { mutableStateOf(defaultAssets) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repair Requests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    val reportText = buildReport(schoolProfile?.schoolName ?: "Unknown School", selectedAssets)
                    shareReport(context, reportText, schoolProfile?.schoolName ?: "Unknown School", selectedAssets)
                }) {
                    Text("Generate SDMC Report")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(selectedAssets) { asset ->
                ListItem(
                    headlineContent = { Text(asset.name) },
                    supportingContent = { Text("Value: ${asset.purchaseValue ?: "N/A"}") },
                    trailingContent = {
                        IconButton(onClick = {
                            selectedAssets = selectedAssets.filter { it.id != asset.id }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                )
            }
        }
    }
}

fun buildReport(schoolName: String, assets: List<AssetEntity>): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val date = sdf.format(Date())
    val sb = java.lang.StringBuilder()
    sb.append("SDMC Repair Request Report\n")
    sb.append("School: $schoolName\n")
    sb.append("Date: $date\n\n")
    sb.append("Items Needing Repair:\n")
    assets.forEach { asset ->
        sb.append("- ${asset.name} (Condition: ${asset.currentCondition.name}) - Value: ${asset.purchaseValue ?: "N/A"}\n")
    }
    return sb.toString()
}

fun shareReport(context: Context, reportText: String, schoolName: String, assets: List<AssetEntity>) {
    val pdfFile = File(context.cacheDir, "RepairRequest.pdf")
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
    val page = document.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()
    
    var y = 50f
    paint.textSize = 20f
    canvas.drawText("SDMC Repair Request Report", 50f, y, paint)
    y += 30f
    paint.textSize = 14f
    canvas.drawText("School: $schoolName", 50f, y, paint)
    y += 20f
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    canvas.drawText("Date: ${sdf.format(Date())}", 50f, y, paint)
    y += 40f
    
    paint.textSize = 16f
    canvas.drawText("Items Needing Repair:", 50f, y, paint)
    y += 20f
    
    paint.textSize = 12f
    assets.forEach { asset ->
        canvas.drawText("- ${asset.name} (Condition: ${asset.currentCondition.name}) - Value: ${asset.purchaseValue ?: "N/A"}", 50f, y, paint)
        y += 20f
    }
    
    document.finishPage(page)
    try {
        document.writeTo(FileOutputStream(pdfFile))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    document.close()

    try {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "SDMC Repair Report - $schoolName")
            putExtra(Intent.EXTRA_TEXT, reportText)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    } catch (e: Exception) {
        // Fallback to text
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "SDMC Repair Report")
            putExtra(Intent.EXTRA_TEXT, reportText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}

