package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PinLockScreen(
    expectedPin: String,
    onUnlock: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter PIN", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(enteredPin.map { '*' }.joinToString(""), style = MaterialTheme.typography.displayMedium)
        
        if (attempts >= 3) {
            Spacer(Modifier.height(16.dp))
            Text("Hint: Ask the primary teacher for the PIN.", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(32.dp))
        
        Column {
            for (row in 0..2) {
                Row {
                    for (col in 1..3) {
                        val digit = row * 3 + col
                        Button(
                            onClick = { 
                                enteredPin += digit
                                if (enteredPin.length == 4) {
                                    if (enteredPin == expectedPin) {
                                        onUnlock()
                                    } else {
                                        attempts++
                                        enteredPin = ""
                                    }
                                }
                             },
                            modifier = Modifier.padding(8.dp).size(64.dp)
                        ) {
                            Text(digit.toString())
                        }
                    }
                }
            }
            Row {
                Spacer(Modifier.padding(8.dp).size(64.dp))
                Button(
                    onClick = { 
                        enteredPin += "0"
                        if (enteredPin.length == 4) {
                            if (enteredPin == expectedPin) {
                                onUnlock()
                            } else {
                                attempts++
                                enteredPin = ""
                            }
                        }
                    },
                    modifier = Modifier.padding(8.dp).size(64.dp)
                ) {
                    Text("0")
                }
                IconButton(
                    onClick = { if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1) },
                    modifier = Modifier.padding(8.dp).size(64.dp)
                ) {
                    Text("CLR")
                }
            }
        }
    }
}

