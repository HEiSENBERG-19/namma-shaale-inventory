package com.example.myapplication.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

// 1. Custom Text Field (For Setup Screen)
@Composable
fun NammaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = label,
                    color = TextHint
                )
            },
            shape = RoundedCornerShape(12.dp),

            // Material 3 compatible colors API
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,

                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = OutlineLight,
                disabledBorderColor = OutlineLight,

                cursorColor = PrimaryBlue,

                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),

            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 2. Dashboard Summary Card (The 2x2 Grid)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.White.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = textColor
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

// 3. Segmented Progress Bar (For Asset Health Card)
@Composable
fun SegmentedProgressBar(
    greenCount: Int,
    orangeCount: Int,
    redCount: Int,
    modifier: Modifier = Modifier
) {
    val total = greenCount + orangeCount + redCount

    Canvas(
        modifier = modifier.height(12.dp)
    ) {

        val w = size.width
        val h = size.height
        val corner = CornerRadius(h / 2, h / 2)

        // Background track
        drawRoundRect(
            color = StatusGrey,
            size = Size(w, h),
            cornerRadius = corner
        )

        if (total > 0) {

            val greenWidth = (greenCount.toFloat() / total) * w
            val orangeWidth = (orangeCount.toFloat() / total) * w
            val redWidth = (redCount.toFloat() / total) * w

            // Draw Red
            drawRoundRect(
                color = StatusRed,
                size = Size(
                    greenWidth + orangeWidth + redWidth,
                    h
                ),
                cornerRadius = corner
            )

            // Draw Orange
            drawRoundRect(
                color = StatusOrange,
                size = Size(
                    greenWidth + orangeWidth,
                    h
                ),
                cornerRadius = corner
            )

            // Draw Green
            drawRoundRect(
                color = StatusGreen,
                size = Size(
                    greenWidth,
                    h
                ),
                cornerRadius = corner
            )
        }
    }
}