package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.*
import com.example.myapplication.viewmodel.AssetViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AssetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SystemBackground
                ) {
                    val schoolProfile by viewModel.schoolProfile.collectAsState()
                    val userPin by viewModel.userPin.collectAsState()

                    var isUnlocked by remember { mutableStateOf(false) }
                    var setupComplete by remember { mutableStateOf(false) }

                    LaunchedEffect(schoolProfile) {
                        if (schoolProfile != null) {
                            setupComplete = true
                        }
                    }

                    if (!setupComplete && schoolProfile == null) {
                        SetupScreen(viewModel = viewModel, onSetupComplete = { setupComplete = true })
                        return@Surface
                    }

                    if (userPin != null && !isUnlocked) {
                        PinLockScreen(expectedPin = userPin!!, onUnlock = { isUnlocked = true })
                        return@Surface
                    }

                    val navController = rememberNavController()

                    // Updated to use the clean Rounded icons
                    val items = listOf(
                        Triple("dashboard", "HOME", Icons.Rounded.Home),
                        Triple("asset_list", "ASSETS", Icons.Rounded.Build),
                        Triple("health_check", "CHECK", Icons.Rounded.CheckCircle),
                        Triple("issues", "ISSUES", Icons.Rounded.Warning),
                        Triple("summary_report", "DOCS", Icons.Rounded.Info)
                    )

                    Scaffold(
                        containerColor = SystemBackground,
                        bottomBar = {
                            // Custom Floating Bottom Navigation Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentRoute = navBackStackEntry?.destination?.route

                                    items.forEach { (route, label, icon) ->
                                        val isSelected = currentRoute == route

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    navController.navigate(route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        if (isSelected) CardBlueBg else Color.Transparent,
                                                        RoundedCornerShape(12.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (isSelected) PrimaryBlue else TextHint,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            if (isSelected) {
                                                Text(
                                                    text = label,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlue,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToAdd = { navController.navigate("add_asset") },
                                    onNavigateToList = { navController.navigate("asset_list") },
                                    onNavigateToReport = { navController.navigate("repair_request") },
                                    onNavigateToHealthCheck = { navController.navigate("health_check") },
                                    onNavigateToIssues = { navController.navigate("issues") }
                                )
                            }
                            composable("asset_list") {
                                AssetListScreen(
                                    viewModel = viewModel,
                                    onNavigateToAdd = { navController.navigate("add_asset") },
                                    onNavigateToDetail = { assetId -> navController.navigate("asset_detail/$assetId") },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("add_asset") {
                                AddAssetScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                            }
                            composable("asset_detail/{assetId}") { backStackEntry ->
                                val assetId = backStackEntry.arguments?.getString("assetId")?.toIntOrNull() ?: 0
                                AssetDetailScreen(assetId = assetId, viewModel = viewModel, onBack = { navController.popBackStack() })
                            }
                            composable("health_check") {
                                HealthCheckScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                            }
                            composable("issues") {
                                IssueLogScreen(
                                    viewModel = viewModel,
                                    navigateToIssueDetail = { issueId -> navController.navigate("issue_detail/$issueId") }
                                )
                            }
                            composable("issue_detail/{issueId}") { backStackEntry ->
                                val issueId = backStackEntry.arguments?.getString("issueId")?.toIntOrNull() ?: 0
                                IssueDetailScreen(issueId = issueId, viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
                            }
                            composable("repair_request") {
                                RepairRequestScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
                            }
                            composable("summary_report") {
                                SummaryReportScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}