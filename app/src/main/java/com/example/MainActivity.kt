package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import com.example.ui.AppNavigation
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("crash_log", "Crash: ${e.message}\n${e.stackTraceToString()}")
                .commit()
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }

        val prefs = getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
        val crashLog = prefs.getString("crash_log", null)
        if (crashLog != null) {
            setContent {
                MyApplicationTheme {
                    androidx.compose.material3.Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        androidx.compose.foundation.layout.Column(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                modifier = androidx.compose.ui.Modifier.size(64.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                            androidx.compose.material3.Text(
                                text = "App Recovery Mode",
                                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.error
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                            androidx.compose.material3.Text(
                                text = "MealMate encountered an unexpected startup issue. You can clear the local data below to restore the app.",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                            androidx.compose.material3.Card(
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                androidx.compose.material3.Text(
                                    text = crashLog,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = androidx.compose.ui.Modifier.padding(16.dp),
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    deleteDatabase("mealmate_database")
                                    getSharedPreferences("mealmate_prefs", android.content.Context.MODE_PRIVATE).edit().clear().commit()
                                    prefs.edit().remove("crash_log").commit()
                                    recreate()
                                },
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                )
                            ) {
                                androidx.compose.material3.Text("Reset App & Start Fresh")
                            }
                        }
                    }
                }
            }
            return
        }

        try {
            setContent {
                MyApplicationTheme {
                    AppNavigation(viewModel = viewModel)
                }
            }
        } catch (e: Exception) {
            setContent {
                MyApplicationTheme {
                    androidx.compose.material3.Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        androidx.compose.foundation.layout.Column(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                modifier = androidx.compose.ui.Modifier.size(64.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                            androidx.compose.material3.Text(
                                text = "App Error",
                                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.error
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                            androidx.compose.material3.Card(
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                androidx.compose.material3.Text(
                                    text = "Error: ${e.message}\n${e.stackTraceToString()}",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = androidx.compose.ui.Modifier.padding(16.dp),
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    deleteDatabase("mealmate_database")
                                    getSharedPreferences("mealmate_prefs", android.content.Context.MODE_PRIVATE).edit().clear().commit()
                                    recreate()
                                },
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                )
                            ) {
                                androidx.compose.material3.Text("Reset App & Start Fresh")
                            }
                        }
                    }
                }
            }
        }
    }
}
