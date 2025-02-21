package com.example.inf2007_project.uam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DependenciesPage(navController: NavController) {
    var dependencies by remember { mutableStateOf(mutableListOf<DependencyData>()) }

    var newDependency by remember { mutableStateOf<DependencyData?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Text("Manage Dependencies", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

            if (dependencies.isEmpty() && newDependency == null) {
                Button(
                    onClick = { newDependency = DependencyData() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Dependency")
                }
            } else {
                dependencies.forEachIndexed { index, dependency ->
                    DependencyForm(
                        dependency = dependency,
                        dependencyNumber = index + 1,
                        isEditableInitially = false,
                        isNewDependency = false,
                        onSave = { updatedDependency ->
                            dependencies = dependencies.toMutableList().apply {
                                this[index] = updatedDependency
                            }
                        },
                        onDelete = {
                            dependencies = dependencies.toMutableList().apply { removeAt(index) }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                newDependency?.let {
                    DependencyForm(
                        dependency = it,
                        dependencyNumber = dependencies.size + 1,
                        isEditableInitially = true,
                        isNewDependency = true,
                        onSave = { savedDependency ->
                            dependencies = dependencies.toMutableList().apply { add(savedDependency) }
                            newDependency = null
                        },
                        onDelete = { newDependency = null }
                    )
                }

                if (newDependency == null) {
                    Button(
                        onClick = { newDependency = DependencyData() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add ${dependencies.size + 1}${getOrdinalSuffix(dependencies.size + 1)} Dependency")
                    }
                }
            }
        }
    }
}

data class DependencyData(
    var nric: String = "",
    var relationship: String = "",
    var phone: String = ""
)

@Composable
fun DependencyForm(
    dependency: DependencyData,
    dependencyNumber: Int,
    isEditableInitially: Boolean = true,
    isNewDependency: Boolean,
    onSave: (DependencyData) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(isEditableInitially) }
    var nric by remember { mutableStateOf(dependency.nric) }
    var relationship by remember { mutableStateOf(dependency.relationship) }
    var phone by remember { mutableStateOf(dependency.phone) }

    var nricError by remember { mutableStateOf(false) }
    var relationshipError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Dependency $dependencyNumber", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))

        if (isEditing) {
            OutlinedTextField(
                value = nric,
                onValueChange = { nric = it },
                label = { Text("NRIC") },
                isError = nricError,
                modifier = Modifier.fillMaxWidth()
            )
            if (nricError) Text("NRIC is required.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = relationship,
                onValueChange = { relationship = it },
                label = { Text("Relationship") },
                isError = relationshipError,
                modifier = Modifier.fillMaxWidth()
            )
            if (relationshipError) Text("Relationship is required.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        phone = it
                        phoneError = it.length != 8
                    } else {
                        phoneError = true
                    }
                },
                label = { Text("Phone Number") },
                isError = phoneError,
                modifier = Modifier.fillMaxWidth()
            )
            if (phoneError) Text("Phone number must be exactly 8 digits.", color = MaterialTheme.colorScheme.error)

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Non-Editable Text
            Text("NRIC: $nric", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Relationship: $relationship", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Phone: $phone", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isEditing) {
                Button(
                    onClick = {
                        nricError = nric.isBlank()
                        relationshipError = relationship.isBlank()
                        phoneError = phone.length != 8

                        if (!nricError && !relationshipError && !phoneError) {
                            onSave(DependencyData(nric, relationship, phone))
                            isEditing = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !phoneError
                ) {
                    Text("Save")
                }
            } else {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isNewDependency) {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete")
            }
        }
    }
}

fun getOrdinalSuffix(number: Int): String {
    return when {
        number in 11..13 -> "th"
        number % 10 == 1 -> "st"
        number % 10 == 2 -> "nd"
        number % 10 == 3 -> "rd"
        else -> "th"
    }
}
