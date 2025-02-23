package com.example.inf2007_project.uam

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import kotlinx.coroutines.tasks.await
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesPage(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var dependencies by remember { mutableStateOf(emptyList<DependencyData>()) }
    var selectedDependency by remember { mutableStateOf<DependencyData?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }


    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("dependencies")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("Firestore Error", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        dependencies = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(DependencyData::class.java)?.let { dependency ->
                                val depId = dependency.dependencyId.takeIf { !it.isNullOrEmpty() } ?: doc.id
                                Log.d("Firestore Data", "Retrieved Dependency: ${dependency.name}, ID: $depId")
                                dependency.copy(dependencyId = depId)
                            }
                        }
                    }
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
        bottomBar = {
            BottomNavigationBar(navController)
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
                    // auto increment for dep
                    dependencyNumber = index + 1,
                    onEdit = { selectedDependency = dependency },
                    navController
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
            Log.d("Selected Dependency",  selectedDependency.toString())

            DependencyEditDialog(
                dependency = selectedDependency!!,
                onDismiss = { selectedDependency = null },
                onSave = { updatedDependency ->
                    // Call function to update only the relationship field
                    updateDependencyRelationship(selectedDependency!!.dependencyId!!, updatedDependency.relationship, firestore)

                    // Update only the relationship field in the local state
                    dependencies = dependencies.map {
                        if (it.dependencyId == updatedDependency.dependencyId) it.copy(relationship = updatedDependency.relationship) else it
                    }
                    Log.d("Update Dependency Test", "Updating relationship: ${updatedDependency.relationship}")
                    selectedDependency = null
                },
                onDelete = {
                    deleteDependencyFromFirestore(selectedDependency!!.dependencyId!!, firestore)
                    dependencies = dependencies.filterNot { it.dependencyId == selectedDependency!!.dependencyId }
                    selectedDependency = null
                }
            )
        }


        if (isAddingNew) {
            DependencyEditDialog(
                dependency = DependencyData(),
                onDismiss = { isAddingNew = false },
                onSave = { newDependency ->
                    addDependencyToFirestore(newDependency, firestore, userId!!, context)
                    //dependencies = dependencies + newDependency
                    isAddingNew = false
                }
            )
        }
    }
}

// display dep information
@Composable
fun DependencyDisplay(dependency: DependencyData, dependencyNumber: Int, onEdit: () -> Unit, navController: NavController) {
    val messageIcon = Icons.Filled.MailOutline

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
        IconButton(onClick = {
            navController.navigate("messages/${dependency.dependencyId}")
            Log.d("Recipient Message", "DependencyId: ${dependency.dependencyId}")
        }) {
            Icon(imageVector = Icons.Filled.MailOutline, contentDescription = "Send Message")
        }

    }
}

// edit dep info
@Composable
fun DependencyEditDialog(
    dependency: DependencyData,
    onDismiss: () -> Unit,
    onSave: (DependencyData) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()



    var name by remember { mutableStateOf(dependency.name) }
    var nric by remember { mutableStateOf(dependency.nric) }
    var relationship by remember { mutableStateOf(dependency.relationship) }
    var phone by remember { mutableStateOf(dependency.phone) }
    var email by remember { mutableStateOf(dependency.email) }
    var dependencyId by remember { mutableStateOf(dependency.dependencyId) }
    var searchedUser by remember { mutableStateOf<DependencyData?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf(false) }
    //var relationship by remember { mutableStateOf("") }
    var mExpanded by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var isEmpty by remember { mutableStateOf(false) }

    val relationshipType = listOf("Child", "Cousin", "Friend")
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (mExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    val initialRelationship = remember { dependency.relationship }  // Store original value

    val isSaveEnabled by remember(initialRelationship, relationship) {
        derivedStateOf {
            relationship.isNotBlank() && relationship != initialRelationship
        }
    }




    fun searchUser() {
        isSearching = true
        searchError = false
        searched = true
        firestore.collection("userDetail")
            .whereEqualTo("nric", nric)
            .get()
            .addOnSuccessListener { documents ->
                isSearching = false
                if (!documents.isEmpty) {
                    val doc = documents.documents.first()
                    searchedUser = doc.toObject(DependencyData::class.java)?.copy(dependencyId = doc.id)
                } else {
                    searchError = true
                    searchedUser = null
                }
            }
            .addOnFailureListener {
                isSearching = false
                searchError = true
            }
    }

    AlertDialog(

        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                if (searchedUser != null && searched) {
                    onSave(
                        dependency.copy(
                            name = searchedUser!!.name,
                            nric = searchedUser!!.nric,
                            relationship = relationship,
                            phone = searchedUser!!.phone,
                            email = searchedUser!!.email,
                            dependencyId = searchedUser!!.dependencyId // Store Elderly UID
                        )
                    )
                }
                else{
                    onSave(
                        dependency.copy(
                            relationship = relationship
                        )
                    )
                }
            },
                // field is not empty, save button enabled
                enabled = isSaveEnabled
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                Button(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
                onDelete?.let {
                    Spacer(modifier = Modifier.width(8.dp))
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
                // NRIC Search Field
                OutlinedTextField(
                    value = nric,
                    onValueChange = { if (it.length <= 9) {
                        nric = it
                    } else {
                        Toast.makeText(context, "NRIC cannot be more than 9 characters", Toast.LENGTH_SHORT).show()
                    }},
                    label = { Text("NRIC") },
                    isError = searchError || isEmpty,
                    supportingText = { if (searchError) Text("No user found with this NRIC")
                                     else if(isEmpty) Text("NRIC Field is Empty")},
                    readOnly = dependency.dependencyId != null // Make it read-only if updating
                )

                Button(
                    onClick = {
                        if(nric.isBlank()){
                            isEmpty = true
                        }
                        else{
                            searchUser()
                        }

                         },
                    enabled = !isSearching && dependency.dependencyId == null
                ) {
                    Text(if (isSearching) "Searching..." else "Search User")
                }

                // Display searched user details if found
                searchedUser?.let { user -> searchedUser?.let { user ->
                    name = user.name
                    nric = user.nric
                    phone = user.phone
                    email = user.email
                }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Elderly Details", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Name: ${user.name}")
                            Text("NRIC: ${user.nric}")
                            Text("Phone: ${user.phone}")
                            Text("Email: ${user.email}")
                            Spacer(modifier = Modifier.height(8.dp))
                            //Text("UID: ${user.id}")
                        }
                    }
                }
                Box {
                    OutlinedTextField(
                        value = relationship,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            //.fillMaxWidth()
                            .clickable { mExpanded = true }
                            .onGloballyPositioned { coordinates -> mTextFieldSize = coordinates.size.toSize() },
                        label = { Text("Relationship") },
                        trailingIcon = { Icon(icon, "Dropdown", Modifier.clickable { mExpanded = !mExpanded }) }
                    )

                    DropdownMenu(
                        expanded = mExpanded,
                        onDismissRequest = { mExpanded = false },
                        modifier = Modifier.width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                    ) {
                        relationshipType.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(text = label) },
                                onClick = {
                                    relationship = label
                                    mExpanded = false
                                }
                            )
                        }
                    }
                }


            }
        }
    )
}


// Below are the firebase code

// firebase update
//fun updateDependencyInFirestore(dependency: DependencyData, firestore: FirebaseFirestore) {
//    dependency.id?.let {
//        firestore.collection("dependencies").document(it).set(dependency)
//        Log.d("Update Dependency", dependency.toString())
//    }
//}

// firebase delete dep
fun deleteDependencyFromFirestore(dependencyId: String, firestore: FirebaseFirestore) {
    firestore.collection("dependencies").document(dependencyId).delete()
    Log.d("Delete Dependency", "Deleted dependency with id: $dependencyId")
}

// firebase add dep
fun addDependencyToFirestore(dependency: DependencyData, firestore: FirebaseFirestore, userId: String, context: Context) {
    firestore.collection("dependencies")
        .whereEqualTo("userId", userId)  // Ensure same user
        .whereEqualTo("dependencyId", dependency.dependencyId)  // Check if dependency already exists
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                // No duplicate found, proceed with adding
                firestore.collection("dependencies")
                    .add(dependency.copy(userId = userId))
                    .addOnSuccessListener { Log.d("Create Dependency", "Dependency created successfully") }
                    .addOnFailureListener { Log.e("Create Dependency Error", "Failed to create dependency: ${it.message}") }
            } else {
                // Dependency already exists, show error message
                Log.e("Create Dependency Error", "Dependency already exists for this user!")
                Toast.makeText(context, "Duplicate Record Found!", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Log.e("Create Dependency Error", "Error checking for duplicates: ${e.message}")
            //Toast.makeText(context, "Duplicate Record Found!", Toast.LENGTH_SHORT).show()
        }
}


fun updateDependencyRelationship(dependencyId: String, newRelationship: String, firestore: FirebaseFirestore) {
    firestore.collection("dependencies").document(dependencyId)
        .update("relationship", newRelationship)
        .addOnSuccessListener {
            Log.d("Update Dependency", "Relationship with $dependencyId updated successfully to: $newRelationship")
        }
        .addOnFailureListener { e ->
            Log.e("Update Dependency Error", "Failed to update relationship: ${e.message}")
        }
}
//fun getDependencyDetails(userId: String, firestore: FirebaseFirestore, onResult: (List<DependencyData>) -> Unit) {
//    firestore.collection("dependencies")
//        .whereEqualTo("userId", userId)
//        .get()
//        .addOnSuccessListener { result ->
//            val dependencies = result.documents.mapNotNull { doc ->
//                doc.toObject(DependencyData::class.java)?.copy(dependencyId = doc.id)
//            }
//            onResult(dependencies)  // Pass the fetched dependencies to the callback
//        }
//        .addOnFailureListener { e ->
//            Log.e("Firestore Error", "Failed to get dependencies: ${e.message}")
//            onResult(emptyList())  // Return an empty list on failure
//        }
//}


// info in the form
data class DependencyData(
    var dependencyId: String? = null, //dependency id --> elderly id
    var userId: String? = null, //caretaker id --> current userid
    var name: String = "",
    var nric: String = "",
    var relationship: String = "",
    var phone: String = "",
    var email: String = ""
)
