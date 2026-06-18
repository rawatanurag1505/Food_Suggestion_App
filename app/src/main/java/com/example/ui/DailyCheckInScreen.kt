package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MealMateDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        // Clickable overlay covering the text field entirely to open the dropdown on touch safely
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCheckInScreen(viewModel: MainViewModel, onNavigateToSuggestions: () -> Unit) {
    val scrollState = rememberScrollState()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.errorMessage.collectAsStateWithLifecycle()
    val navigate by viewModel.navigateToSuggestions.collectAsStateWithLifecycle()

    var mood by remember { mutableStateOf("Healthy") }
    var mealType by remember { mutableStateOf("Dinner") }
    var timeAvailable by remember { mutableStateOf("30 min") }
    var budget by remember { mutableStateOf("") }

    LaunchedEffect(navigate) {
        if (navigate) {
            viewModel.onNavigatedToSuggestions()
            onNavigateToSuggestions()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "What are you craving?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = MaterialTheme.typography.headlineLarge.lineHeight
            )

            Text(
                "Tell us a bit about right now, and we'll suggest the perfect meal.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            val mealTypeOptions = listOf("Breakfast", "Brunch", "Lunch", "Snack", "Dinner", "Dessert")
            MealMateDropdown(
                label = "Meal Type",
                selectedValue = mealType,
                options = mealTypeOptions,
                onSelected = { mealType = it },
                modifier = Modifier.fillMaxWidth()
            )

            val moodOptions = listOf("Comforting", "Light & Fresh", "Spicy", "Sweet", "Savory", "Healthy", "Surprise Me")
            MealMateDropdown(
                label = "Current Mood",
                selectedValue = mood,
                options = moodOptions,
                onSelected = { mood = it },
                modifier = Modifier.fillMaxWidth()
            )

            val timeOptions = listOf("15 min", "30 min", "45 min", "1 hour", "2+ hours")
            MealMateDropdown(
                label = "Time Available",
                selectedValue = timeAvailable,
                options = timeOptions,
                onSelected = { timeAvailable = it },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("Budget constraints (Optional)") },
                placeholder = { Text("e.g. ₹200") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            Button(
                onClick = { 
                    viewModel.generateSuggestions(mood, timeAvailable, budget, mealType) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Suggest Meals", fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
