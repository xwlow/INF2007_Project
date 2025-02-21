package com.example.inf2007_project.uam

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesPage(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var dependencies by remember { mutableStateOf(emptyList<DependencyData>()) }
    var selectedDependency by remember { mutableStateOf<DependencyData?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val result = firestore.collection("dependencies")
                    .whereEqualTo("user_id", userId)
                    .get().await()
                dependencies = result.documents.mapNotNull { doc ->
                    doc.toObject(DependencyData::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                // if failed to get info
                println("Error fetching dependencies: ${e.message}")
            }
        }
    }

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
                DependencyDisplay(
                    dependency = dependency,
                    // auto increament for dep
                    dependencyNumber = index + 1,
                    onEdit = { selectedDependency = dependency }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // add dep btn
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { isAddingNew = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Dependency")
            }
        }

        // selected dep info & features
        if (selectedDependency != null) {
            DependencyEditDialog(
                dependency = selectedDependency!!,
                onDismiss = { selectedDependency = null },
                onSave = { updatedDependency ->
                    updateDependencyInFirestore(updatedDependency, firestore)
                    dependencies = dependencies.map { if (it.id == updatedDependency.id) updatedDependency else it }
                    selectedDependency = null
                },
                onDelete = {
                    deleteDependencyFromFirestore(selectedDependency!!.id!!, firestore)
                    dependencies = dependencies.filterNot { it.id == selectedDependency!!.id }
                    selectedDependency = null
                }
            )
        }

        if (isAddingNew) {
            DependencyEditDialog(
                dependency = DependencyData(),
                onDismiss = { isAddingNew = false },
                onSave = { newDependency ->
                    addDependencyToFirestore(newDependency, firestore, userId!!)
                    dependencies = dependencies + newDependency
                    isAddingNew = false
                }
            )
        }
    }
}

// display dep information
@Composable
fun DependencyDisplay(dependency: DependencyData, dependencyNumber: Int, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Dependency $dependencyNumber", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Name: ${dependency.name}")
            Text("NRIC: ${dependency.nric}")
            Text("Relationship: ${dependency.relationship}")
            Text("Phone: ${dependency.phone}")
            Text("Email: ${dependency.email}")
        }
    }
}

// edit dep info
@Composable
fun DependencyEditDialog(
    dependency: DependencyData,
    onDismiss: () -> Unit,
    onSave: (DependencyData) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(dependency.name) }
    var nric by remember { mutableStateOf(dependency.nric) }
    var relationship by remember { mutableStateOf(dependency.relationship) }
    var phone by remember { mutableStateOf(dependency.phone) }
    var email by remember { mutableStateOf(dependency.email) }

    // error handling. checking for valid inputs
    var nameError by remember { mutableStateOf(false) }
    var nricError by remember { mutableStateOf(false) }
    var relationshipError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    fun validateInputs(): Boolean {
        nameError = name.isBlank()
        nricError = nric.isBlank()
        relationshipError = relationship.isBlank()
        // ONLY 8 digits
        phoneError = phone.length != 8
        // ONLY accepts email format
        emailError = !Patterns.EMAIL_ADDRESS.matcher(email).matches()

        return !(nameError || nricError || relationshipError || phoneError || emailError)
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            // save button for EDIT
            Button(onClick = {
                if (validateInputs()) {
                    onSave(dependency.copy(name = name, nric = nric, relationship = relationship, phone = phone, email = email))
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                // cancel button for EDIT
                Button(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
                onDelete?.let {
                    Spacer(modifier = Modifier.width(8.dp))

                    // delete button to remove dep
                    Button(
                        onClick = { onDelete() },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Please enter a name") }
                )
                OutlinedTextField(
                    value = nric,
                    onValueChange = { nric = it },
                    label = { Text("NRIC") },
                    isError = nricError,
                    supportingText = { if (nricError) Text("Please enter NRIC") }
                )
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship") },
                    isError = relationshipError,
                    supportingText = { if (relationshipError) Text("Please enter relationship") }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.all { char -> char.isDigit() }) phone = it },
                    label = { Text("Phone") },
                    //ensures that only accept NUMBER
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    isError = phoneError,
                    supportingText = { if (phoneError) Text("Phone must be 8 digits") }
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    isError = emailError,
                    supportingText = { if (emailError) Text("Please enter a valid email") }
                )
            }
        }
    )
}

// Below are the firebase code

// firebase update
fun updateDependencyInFirestore(dependency: DependencyData, firestore: FirebaseFirestore) {
    dependency.id?.let {
        firestore.collection("dependencies").document(it).set(dependency)
    }
}

// firebase delete dep
fun deleteDependencyFromFirestore(id: String, firestore: FirebaseFirestore) {
    firestore.collection("dependencies").document(id).delete()
}

// firebase add dep
fun addDependencyToFirestore(dependency: DependencyData, firestore: FirebaseFirestore, userId: String) {
    firestore.collection("dependencies").add(dependency.copy(user_id = userId))
}

// info in the form
data class DependencyData(
    var id: String? = null,
    var user_id: String? = null,
    var name: String = "",
    var nric: String = "",
    var relationship: String = "",
    var phone: String = "",
    var email: String = ""
)
