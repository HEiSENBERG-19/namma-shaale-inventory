package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AssetViewModel

@Composable
fun SetupScreen(
    viewModel: AssetViewModel,
    onSetupComplete: () -> Unit
) {
    var schoolName by remember { mutableStateOf("") }
    var diseCode by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var block by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var teacherPin by remember { mutableStateOf("") }

    val isFormValid = schoolName.isNotBlank() && district.isNotBlank() && block.isNotBlank() && teacherName.isNotBlank()

    Surface(color = SystemBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Icon Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(CardBlueBg, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Home, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to Namma-Shaale",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )

            Text(
                text = "Set up your school profile to get started with inventory tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            NammaTextField(value = schoolName, onValueChange = { schoolName = it }, label = "School Name *")
            Spacer(modifier = Modifier.height(16.dp))

            NammaTextField(value = diseCode, onValueChange = { diseCode = it }, label = "DISE Code")
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                NammaTextField(value = district, onValueChange = { district = it }, label = "District *", modifier = Modifier.weight(1f))
                NammaTextField(value = block, onValueChange = { block = it }, label = "Block *", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            NammaTextField(value = teacherName, onValueChange = { teacherName = it }, label = "Your Name (Teacher) *")
            Spacer(modifier = Modifier.height(16.dp))

            // For a PIN, you might want visual dots, but standard text field matches the layout for now.
            NammaTextField(value = teacherPin, onValueChange = { teacherPin = it }, label = "Create 4-digit PIN *")

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    viewModel.setActiveTeacher(teacherName)
                    viewModel.setUserPin(teacherPin)
                    viewModel.saveSchoolProfile(schoolName, diseCode, district, block)
                    onSetupComplete()
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = OutlineLight
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Complete Setup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isFormValid) Color.White else TextHint)
            }
        }
    }
}