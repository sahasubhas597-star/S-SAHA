package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.screens.AiCopilotScreen
import com.example.ui.screens.BacktestLabScreen
import com.example.ui.screens.BrokerIntegrationScreen
import com.example.ui.screens.MarketScannerScreen
import com.example.ui.screens.MarketWorkstationScreen
import com.example.ui.screens.OptionChainScreen
import com.example.ui.screens.PaperTradingScreen
import com.example.ui.screens.PortfolioRiskScreen
import com.example.ui.screens.StrategyBuilderScreen
import com.example.ui.screens.Zx26WebAdminScreen
import com.example.ui.theme.AlgoTradingHubTheme
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlgoTradingHubTheme {
                val viewModel: MainViewModel = viewModel()
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    val navTabs = listOf(
        NavTabItem("ZX26 Web", Icons.Default.Language, "nav_zx26_web"),
        NavTabItem("Workstation", Icons.Default.ShowChart, "nav_workstation"),
        NavTabItem("F&O Options", Icons.Default.Layers, "nav_options"),
        NavTabItem("Scanner", Icons.Default.Radar, "nav_scanner"),
        NavTabItem("Strategy", Icons.Default.Tune, "nav_strategy"),
        NavTabItem("Backtest", Icons.Default.HistoryEdu, "nav_backtest"),
        NavTabItem("Paper Trade", Icons.Default.AccountBalanceWallet, "nav_paper"),
        NavTabItem("Portfolio", Icons.Default.PieChart, "nav_portfolio"),
        NavTabItem("Brokers", Icons.Default.Cable, "nav_brokers"),
        NavTabItem("AI Copilot", Icons.Default.Psychology, "nav_ai")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_zx26_logo_1787908955898),
                                contentDescription = "ZX26 Logo",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, NeonCyan, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "zx26",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BrightGold.copy(alpha = 0.2f))
                                    .border(1.dp, BrightGold.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = BrightGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Admin: zx26",
                                        color = BrightGold,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TerminalSurfaceElevated)
                                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "24ms • 9 EXCHANGES",
                                    color = NeonCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setTab(1) },
                        modifier = Modifier.testTag("top_bar_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Ticker Feed",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TerminalBg
                )
            )
        },
        bottomBar = {
            ScrollableTabRow(
                selectedTabIndex = currentTab,
                containerColor = TerminalSurface,
                contentColor = NeonCyan,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                        color = NeonCyan,
                        height = 3.dp
                    )
                },
                divider = {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TerminalCardBorder))
                }
            ) {
                navTabs.forEachIndexed { index, tabItem ->
                    val isSelected = currentTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setTab(index) },
                        text = {
                            Text(
                                text = tabItem.title,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tabItem.icon,
                                contentDescription = tabItem.title,
                                tint = if (isSelected) NeonCyan else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag(tabItem.testTag)
                    )
                }
            }
        },
        containerColor = TerminalBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TerminalBg)
        ) {
            when (currentTab) {
                0 -> Zx26WebAdminScreen(viewModel = viewModel)
                1 -> MarketWorkstationScreen(viewModel = viewModel)
                2 -> OptionChainScreen(viewModel = viewModel)
                3 -> MarketScannerScreen(viewModel = viewModel)
                4 -> StrategyBuilderScreen(viewModel = viewModel)
                5 -> BacktestLabScreen(viewModel = viewModel)
                6 -> PaperTradingScreen(viewModel = viewModel)
                7 -> PortfolioRiskScreen(viewModel = viewModel)
                8 -> BrokerIntegrationScreen(viewModel = viewModel)
                9 -> AiCopilotScreen(viewModel = viewModel)
            }
        }
    }
}
