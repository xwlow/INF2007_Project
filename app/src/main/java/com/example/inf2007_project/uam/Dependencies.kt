package com.example.inf2007_project.uam

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesPage(navController: NavController) {
    var dependencies by remember { mutableStateOf(mutableListOf<DependencyData>()) }
    var newDependency by remember { mutableStateOf<DependencyData?>(null) }
    var isEditingDependency by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Dependencies") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
            dependencies.forEachIndexed { index, dependency ->
                DependencyForm(
                    dependency = dependency,
                    dependencyNumber = index + 1,
                    isEditableInitially = false,
                    isNewDependency = false,
                    onSave = { updatedDependency ->
                        dependencies = dependencies.toMutableList().apply { this[index] = updatedDependency }
                        isEditingDependency = false
                    },
                    onDelete = {
                        dependencies = dependencies.toMutableList().apply { removeAt(index) }
                        isEditingDependency = false
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // dep form
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
                    onCancel = { newDependency = null }
                )
            }

            // add dep
            if (!isEditingDependency && newDependency == null) {
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

data class DependencyData(
    var name: String = "",
    var nric: String = "",
    var relationship: String = "",
    var phone: String = "",
    var email: String = ""
)

// single dep form
@Composable
fun DependencyForm(
    dependency: DependencyData,
    dependencyNumber: Int,
    isEditableInitially: Boolean = true,
    isNewDependency: Boolean,
    onSave: (DependencyData) -> Unit,
    onDelete: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    // normal form
    var isEditing by remember { mutableStateOf(isEditableInitially) }
    var name by remember { mutableStateOf(dependency.name) }
    var nric by remember { mutableStateOf(dependency.nric) }
    var relationship by remember { mutableStateOf(dependency.relationship) }
    var phone by remember { mutableStateOf(dependency.phone) }
    var email by remember { mutableStateOf(dependency.email) }

    // error handling
    var nameError by remember { mutableStateOf(false) }
    var nricError by remember { mutableStateOf(false) }
    var relationshipError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Dependency $dependencyNumber", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))

        // name field
        if (isEditing) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                isError = nameError,
                modifier = Modifier.fillMaxWidth()
            )

            //name error
            if (nameError) Text("Name is required.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            // nric field
            OutlinedTextField(
                value = nric,
                onValueChange = { nric = it },
                label = { Text("NRIC") },
                isError = nricError,
                modifier = Modifier.fillMaxWidth()
            )

            // nric error
            if (nricError) Text("NRIC is required.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            // relationship
            OutlinedTextField(
                value = relationship,
                onValueChange = { relationship = it },
                label = { Text("Relationship") },
                isError = relationshipError,
                modifier = Modifier.fillMaxWidth()
            )

            // relationship error
            if (relationshipError) Text("Relationship is required.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            // phoneNumber
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

            // phoneNumber error
            if (phoneError) Text("Phone number must be exactly 8 digits.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            // email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                },
                label = { Text("Email") },
                isError = emailError,
                modifier = Modifier.fillMaxWidth()
            )

            // email error
            if (emailError) Text("Invalid email format.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            // save & cancel buttons for EDIT mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        nameError = name.isBlank()
                        nricError = nric.isBlank()
                        relationshipError = relationship.isBlank()
                        phoneError = phone.length != 8
                        emailError = !Patterns.EMAIL_ADDRESS.matcher(email).matches()

                        if (!nameError && !nricError && !relationshipError && !phoneError && !emailError) {
                            onSave(DependencyData(name, nric, relationship, phone, email))
                            isEditing = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !phoneError && !emailError
                ) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onCancel?.invoke() },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        } else {
            Text("Name: $name", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))

            Text("NRIC: $nric", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Relationship: $relationship", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Phone: $phone", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Email: $email", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // edit & delete button for DISPLAY mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onDelete?.invoke() },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

// dynamic ordinal suffix
fun getOrdinalSuffix(number: Int): String {
    return when {
        number in 11..13 -> "th"
        number % 10 == 1 -> "st"
        number % 10 == 2 -> "nd"
        number % 10 == 3 -> "rd"
        else -> "th"
    }
}
