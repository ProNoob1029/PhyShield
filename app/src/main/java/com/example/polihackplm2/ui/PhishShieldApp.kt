package com.example.polihackplm2.ui

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.polihackplm2.functionality.HomeRefreshManager
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PhishShieldApp(
    viewModel: PhishShieldViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle events from ViewModel (like auto-block notifications)
    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val pendingUrl by viewModel.pendingUrl.collectAsState()
    LaunchedEffect(pendingUrl) {
        pendingUrl?.let { url ->
            viewModel.startScan(url, context)
            viewModel.consumePendingUrl()
            navController.navigate("alert")
        }
    }

    val pagerState = rememberPagerState(pageCount = { 4 })

    var showFullscreenImage by remember { mutableStateOf(false) }
    var fullscreenBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val isMainScreen = currentRoute == "main" || currentRoute == null

    // Handle system back button navigation
    BackHandler(enabled = showFullscreenImage || (currentRoute != "main" && currentRoute != null) || pagerState.currentPage != 0) {
        if (showFullscreenImage) {
            showFullscreenImage = false
            fullscreenBitmap = null
        } else if (currentRoute != "main" && currentRoute != null) {
//            navController.popBackStack()
        } else {
            coroutineScope.launch {
                pagerState.animateScrollToPage(0)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Ignore system insets to handle them manually
        bottomBar = {
            if (isMainScreen) {
                PhishShieldBottomNav(
                    selectedTab = pagerState.currentPage,
                    onTabSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (showFullscreenImage && fullscreenBitmap != null) {
                FullscreenImageScreen(
                    bitmap = fullscreenBitmap!!,
                    onDismiss = {
                        showFullscreenImage = false
                        fullscreenBitmap = null
                    }
                )
            } else {
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                            HorizontalPager(
                                modifier = Modifier.fillMaxSize(),
                                state = pagerState,
                                beyondViewportPageCount = 1 // Pre-load neighboring screens for stability
                            ) { page ->
                                when (page) {
                                    0 -> HomeScreen(
                                        onThreatClick = { clickedThreat ->
                                            viewModel.setCurrentThreat(clickedThreat)
                                            navController.navigate("detail")
                                        })
                                    1 -> ScannerScreen(
                                        onScanTriggered = { url ->
                                            viewModel.startScan(url, context)
                                            navController.navigate("alert")
                                        })
                                    2 -> LogScreen(
                                        onBlockedItemClick = { entity ->
                                            viewModel.setSelectedBlockedDomain(entity.domain)
                                            viewModel.setSelectedEntity(entity)
                                            navController.navigate("blockedDetails")
                                        })
                                    3 -> SettingsScreen()
                                }
                            }
                        }
                    }
                    composable("detail") {
                        val currentThreat by viewModel.currentThreat.collectAsState()
                        if (currentThreat != null) {
                            ScanDetailScreen(
                                threat = currentThreat!!,
                                onBack = { navController.popBackStack() },
                                onOpenUrl = { url ->
                                    viewModel.setCurrentUrl(url)
                                    navController.navigate("safeWebView")
                                },
                                onDelete = {
                                    HomeRefreshManager.deleteScanResult(currentThreat!!.id) {
                                        navController.popBackStack()
                                    }
                                },
                                onScreenshotClick = { bitmap ->
                                    fullscreenBitmap = bitmap
                                    showFullscreenImage = true
                                }
                            )
                        }
                    }
                    composable("safeWebView") {
                        val currentUrl by viewModel.currentUrl.collectAsState()
                        SafeWebViewScreen(
                            url = currentUrl,
                            onBack = {
                                //viewModel.setCurrentUrl("")
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("alert") {
                        val scanState by viewModel.scanState.collectAsState()
                        AlertScreen(
                            scanState = scanState,
                            onBack = {
                                navController.popBackStack()
                            },
                            onOpenUrl = {
                                val url = viewModel.currentUrl.value
                                viewModel.setCurrentUrl(url)
                                navController.navigate("safeWebView") {
                                }
                            },
                            onScreenshotClick = { bitmap ->
                                fullscreenBitmap = bitmap
                                showFullscreenImage = true
                            }
                        )
                    }
                    composable("blockedDetails") {
                        val entity by viewModel.selectedEntity.collectAsStateWithLifecycle()

                        if (entity != null) {
                            BlockedDetailsScreen(
                                entity = entity!!,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack(route = "main", inclusive = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AppPreview() { 
    PhishShieldApp() 
}
