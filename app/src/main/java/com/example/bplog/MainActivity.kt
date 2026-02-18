package com.example.bplog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bplog.data.AppDatabase
import com.example.bplog.data.MeasurementRepository
import com.example.bplog.ui.AddMeasurementViewModel
import com.example.bplog.ui.HistoryViewModel
import com.example.bplog.ui.ViewModelFactory
import com.example.bplog.ui.screen.AddMeasurementScreen
import com.example.bplog.ui.screen.HistoryScreen
import com.example.bplog.ui.theme.BPLogTheme
import com.example.bplog.ui.screen.AboutScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BPLogTheme {
                val context = LocalContext.current
                val repository = remember {
                    MeasurementRepository(
                        AppDatabase.getInstance(context).measurementDao()
                    )
                }
                val viewModelFactory = remember { ViewModelFactory(repository) }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                var menuOpen by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = { Text("BPLog", style = MaterialTheme.typography.titleMedium) },
                            actions = {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(
                                    expanded = menuOpen,
                                    onDismissRequest = { menuOpen = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("About") },
                                        onClick = {
                                            menuOpen = false
                                            navController.navigate("about")
                                        }
                                    )
                                }
                            }
                        )
                    },


                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == "add" } == true,
                                onClick = {
                                    navController.navigate("add") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                label = { Text("Log") }
                            )
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == "history" } == true,
                                onClick = {
                                    navController.navigate("history") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.History, contentDescription = null) },
                                label = { Text("History") }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "add",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("about") { AboutScreen() }

                        composable("add") {
                            val addViewModel: AddMeasurementViewModel = viewModel(
                                factory = viewModelFactory
                            )
                            AddMeasurementScreen(
                                viewModel = addViewModel,
                                onShowSnackbar = { message ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            )
                        }
                        composable("history") {
                            val historyViewModel: HistoryViewModel = viewModel(
                                factory = viewModelFactory
                            )
                            HistoryScreen(viewModel = historyViewModel)
                        }
                    }
                }
            }
        }
    }
}
