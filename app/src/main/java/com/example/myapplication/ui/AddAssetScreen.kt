// AddAssetScreen.kt
package com.example.myapplication.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.myapplication.data.Category
import com.example.myapplication.data.Condition
import com.example.myapplication.viewmodel.AssetViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    viewModel: AssetViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(Condition.GREEN) }
    var category by remember { mutableStateOf(Category.OTHER) }
    var expandedCategory by remember { mutableStateOf(false) }
    var purchaseValue by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val activeTeacher by viewModel.activeTeacher.collectAsState()
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) photoUri = tempUri
    }

    Scaffold(
        containerColor = SystemBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add New Asset", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SystemBackground)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)) {

            // Photo Area
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundElevated),
                onClick = {
                    val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    tempUri = uri
                    cameraLauncher.launch(uri)
                }
            ) {
                if (photoUri != null) {
                    AsyncImage(model = photoUri, contentDescription = "Photo", modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                } else {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Photo", modifier = Modifier.size(40.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Capture Photo", color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Inputs
            CustomTextField(value = name, onValueChange = { name = it }, label = "Asset Name (e.g. Microscope)")
            Spacer(Modifier.height(12.dp))
            CustomTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = "Serial Number")
            Spacer(Modifier.height(12.dp))
            CustomTextField(value = purchaseValue, onValueChange = { purchaseValue = it }, label = "Purchase Value (₹)")
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
                CustomTextField(
                    value = category.name, onValueChange = {}, label = "Category",
                    modifier = Modifier.menuAnchor().fillMaxWidth(), readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) }
                )
                ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }, modifier = Modifier.background(CardBackground)) {
                    Category.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name, color = TextPrimary) },
                            onClick = { category = cat; expandedCategory = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    viewModel.addAsset(
                        name = name,
                        category = category,
                        serialNumber = serialNumber,
                        acquisitionDate = date,
                        purchaseValue = purchaseValue.toDoubleOrNull(),
                        photoPath = null,
                        photoUri = photoUri?.toString(),
                        condition = condition,
                        recordedBy = activeTeacher ?: "Unknown"
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = name.isNotBlank()
            ) {
                Text("Save Asset", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier.fillMaxWidth(), readOnly: Boolean = false, trailingIcon: @Composable (() -> Unit)? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextTertiary) },
        modifier = modifier,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = CardBackground,
            focusedContainerColor = CardBackground,
            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedBorderColor = AccentBlue,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}