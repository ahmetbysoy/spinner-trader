package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BacktestHistoryScreen
import com.example.ui.screens.MarketScreenerScreen
import com.example.ui.screens.MicrostructureScreen
import com.example.ui.screens.QuantStrategyScreen
import com.example.ui.theme.CryptoQuantTheme
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.QuantGold
import com.example.ui.theme.SurfaceCharcoal
import com.example.ui.viewmodel.MarketViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoQuantTheme {
                MainAppContainer()
            }
        }
    }
}

sealed class NavItem(val title: String, val icon: ImageVector, val route: String) {
    object Microstructure : NavItem("Order Book", Icons.Default.ShowChart, "microstructure")
    object Strategies : NavItem("Strategies", Icons.Default.Psychology, "strategies")
    object Screener : NavItem("Screener", Icons.Default.GridView, "screener")
    object Audit : NavItem("Audit Log", Icons.Default.Assessment, "audit")
}

@Composable
fun MainAppContainer(
    viewModel: MarketViewModel = viewModel()
) {
    var selectedRoute by remember { mutableStateOf(NavItem.Microstructure.route) }

    val navItems = listOf(
        NavItem.Microstructure,
        NavItem.Strategies,
        NavItem.Screener,
        NavItem.Audit
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCharcoal,
                contentColor = CyanAccent,
                modifier = Modifier.navigationBarsPadding()
            ) {
                navItems.forEach { item ->
                    val isSelected = selectedRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedRoute = item.route },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyanAccent.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedRoute) {
                NavItem.Microstructure.route -> MicrostructureScreen(viewModel = viewModel)
                NavItem.Strategies.route -> QuantStrategyScreen(viewModel = viewModel)
                NavItem.Screener.route -> MarketScreenerScreen(viewModel = viewModel)
                NavItem.Audit.route -> BacktestHistoryScreen(viewModel = viewModel)
            }
        }
    }
}
