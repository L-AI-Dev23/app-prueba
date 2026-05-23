package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SyncScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.IOSBlue
import com.example.ui.theme.IOSGray
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val financeViewModel: FinanceViewModel = viewModel()
    val currentUser by financeViewModel.currentUser.collectAsState()
    var authScreen by remember { mutableStateOf("login") }

    if (currentUser == null) {
        if (authScreen == "login") {
            LoginScreen(
                viewModel = financeViewModel,
                onNavigateToRegister = { authScreen = "register" },
                onLoginSuccess = {
                    // Navigate automatically to primary screen
                }
            )
        } else {
            RegisterScreen(
                viewModel = financeViewModel,
                onNavigateToLogin = { authScreen = "login" },
                onRegisterSuccess = {
                    // Navigate automatically to primary screen
                }
            )
        }
    } else {
        var selectedTab by remember { mutableStateOf(0) }

        val tabs = listOf(
            TabItem("Resumen", Icons.Default.Home),
            TabItem("Historial", Icons.Default.ReceiptLong),
            TabItem("Presupuestos", Icons.Default.AccountBalanceWallet),
            TabItem("Nube", Icons.Default.CloudSync)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IOSBlue,
                                selectedTextColor = IOSBlue,
                                unselectedIconColor = IOSGray.copy(alpha = 0.8f),
                                unselectedTextColor = IOSGray.copy(alpha = 0.8f),
                                indicatorColor = IOSBlue.copy(alpha = 0.08f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> DashboardScreen(viewModel = financeViewModel)
                    1 -> TransactionsScreen(viewModel = financeViewModel)
                    2 -> BudgetsScreen(viewModel = financeViewModel)
                    3 -> SyncScreen(viewModel = financeViewModel)
                }
            }
        }
    }
}

data class TabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
