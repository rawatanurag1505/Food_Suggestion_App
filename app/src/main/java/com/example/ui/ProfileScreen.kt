package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.db.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectGroup(
    title: String,
    options: List<String>,
    selectedItemsString: String,
    onItemsChanged: (String) -> Unit,
    placeholderText: String = "Add custom...",
    modifier: Modifier = Modifier
) {
    val selectedItems = remember(selectedItemsString) {
        selectedItemsString.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
    
    var customText by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // FlowRow of chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = selectedItems.contains(option)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSet = if (isSelected) {
                                selectedItems - option
                            } else {
                                selectedItems + option
                            }
                            onItemsChanged(newSet.joinToString(", "))
                        },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            
            // If there are other custom entries that are not in predefined options, display them as deletable input chips!
            val customItems = selectedItems.filter { !options.contains(it) }
            if (customItems.isNotEmpty()) {
                Text(
                    text = "Custom selections:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customItems.forEach { customItem ->
                        InputChip(
                            selected = true,
                            onClick = {
                                val newSet = selectedItems - customItem
                                onItemsChanged(newSet.joinToString(", "))
                            },
                            label = { Text(customItem) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Custom item entry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = { Text(placeholderText) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Button(
                    onClick = {
                        val trimmed = customText.trim()
                        if (trimmed.isNotEmpty()) {
                            val updatedSet = selectedItems + trimmed
                            onItemsChanged(updatedSet.joinToString(", "))
                            customText = ""
                        }
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var restrictions by remember { mutableStateOf("") }
    var cuisines by remember { mutableStateOf("") }
    var healthGoal by remember { mutableStateOf("") }
    var skillLevel by remember { mutableStateOf("Intermediate") }
    var equipment by remember { mutableStateOf("") }
    var dislikes by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            restrictions = it.restrictions
            cuisines = it.cuisines
            healthGoal = it.healthGoal
            skillLevel = it.skillLevel
            equipment = it.equipment
            dislikes = it.dislikes
        }
    }

    val dietaryOptions = listOf(
        "Vegan", "Vegetarian", "Pescatarian", "Gluten-Free", "Dairy-Free", 
        "Keto", "Paleo", "Low-Carb", "Nut-Free", "Halal", "Kosher"
    )

    val healthGoalOptions = listOf(
        "Weight Loss", "Muscle Gain", "High Protein", "Low Sodium", 
        "Heart Healthy", "Diabetic Friendly", "Low Sugar", "Clean Eating"
    )

    val kitchenEquipmentOptions = listOf(
        "Stove", "Oven", "Microwave", "Air Fryer", "Blender", 
        "Slow Cooker", "Instant Pot", "Toaster", "Food Processor"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "My Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "Customize dietary preferences, kitchen setup, and cooking skill levels for tailored recommendations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            MultiSelectGroup(
                title = "Dietary Restrictions",
                options = dietaryOptions,
                selectedItemsString = restrictions,
                onItemsChanged = { restrictions = it },
                placeholderText = "e.g. Soy-free"
            )

            OutlinedTextField(
                value = cuisines,
                onValueChange = { cuisines = it },
                label = { Text("Favorite Cuisines") },
                placeholder = { Text("Italian, Indian, Mexican...") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            MultiSelectGroup(
                title = "Health Goals",
                options = healthGoalOptions,
                selectedItemsString = healthGoal,
                onItemsChanged = { healthGoal = it },
                placeholderText = "e.g. Low carb"
            )

            var skillExpanded by remember { mutableStateOf(false) }
            val skillOptions = listOf("Beginner", "Intermediate", "Advanced", "Professional Chef")

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = skillLevel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cooking Skill Level") },
                    trailingIcon = {
                        Icon(
                            imageVector = if (skillExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (skillExpanded) "Collapse" else "Expand"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { skillExpanded = true }
                )
                DropdownMenu(
                    expanded = skillExpanded,
                    onDismissRequest = { skillExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    skillOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                skillLevel = option
                                skillExpanded = false
                            }
                        )
                    }
                }
            }

            MultiSelectGroup(
                title = "Kitchen Equipment",
                options = kitchenEquipmentOptions,
                selectedItemsString = equipment,
                onItemsChanged = { equipment = it },
                placeholderText = "e.g. Wok, Griddle"
            )

            OutlinedTextField(
                value = dislikes,
                onValueChange = { dislikes = it },
                label = { Text("Disliked Foods") },
                placeholder = { Text("Mushrooms, Cilantro...") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Button(
                onClick = {
                    viewModel.saveProfile(
                        UserProfile(
                            id = 1,
                            restrictions = restrictions,
                            cuisines = cuisines,
                            healthGoal = healthGoal,
                            skillLevel = skillLevel,
                            equipment = equipment,
                            dislikes = dislikes
                        )
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("Profile saved successfully!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile", fontSize = MaterialTheme.typography.titleMedium.fontSize)
            }
        }
    }
}
