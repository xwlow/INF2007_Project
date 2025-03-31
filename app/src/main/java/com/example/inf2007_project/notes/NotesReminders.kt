package com.example.inf2007_project.notes

import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotesRemindersPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val documents = remember { mutableStateListOf<Triple<String, String, String>>() }
    val notes = remember { mutableStateListOf<Triple<String, String, String>>() }
    var isDialogOpen by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUser != null) {
            val documentsResult = firestore.collection("documents")
                .whereEqualTo("user_id", currentUser)
                .get()
                .await()
            documents.clear()
            documents.addAll(documentsResult.documents.mapNotNull { doc ->
                val title = doc.getString("title")
                val lastUpdated = doc.getString("lastUpdated")
                val id = doc.id
                if (title != null && lastUpdated != null) Triple(id, title, lastUpdated) else null
            })

            val notesResult = firestore.collection("notes")
                .whereEqualTo("user_id", currentUser)
                .get()
                .await()
            notes.clear()
            notes.addAll(notesResult.documents.mapNotNull { doc ->
                val title = doc.getString("title")
                val lastUpdated = doc.getString("lastUpdated")
                val id = doc.id
                if (title != null && lastUpdated != null) Triple(id, title, lastUpdated) else null
            })
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isDialogOpen = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Notes, Documents and Reminders",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SectionHeader("Documents")
            LazyColumn(
                modifier = Modifier.weight(1f),
                userScrollEnabled = true
            ) {
                items(documents, key = { it.first }) { (id, title, lastUpdated) ->
                    DocumentItem(title, "Last updated on $lastUpdated", id, navController = navController)
                }
            }
            SectionHeader("Notes")
            NotesTabs(selectedTabIndex) { newIndex -> selectedTabIndex = newIndex }
            LazyColumn(
                modifier = Modifier.weight(1f),
                userScrollEnabled = true
            ) {

                val sortedNotes = getSortedNotes(notes, selectedTabIndex)
                items(sortedNotes, key = { it.first }) { (id, title, lastUpdated) ->
                    NoteItem(title, "Last updated on $lastUpdated", id, navController)
                }
            }
        }
        if (isDialogOpen) {
            CardDialog(
                onDismiss = { isDialogOpen = false },
                onAddSuccess = { refreshTrigger++ }
            )
        }
    }
}






@Composable
fun NotesTabs(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Alphabetical", "Date Created", "Date Updated")

    TabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(text = title) }
            )
        }
    }
}

fun getSortedNotes(notes: List<Triple<String, String, String>>, selectedTabIndex: Int): List<Triple<String, String, String>> {
    return when (selectedTabIndex) {
        0 -> notes.sortedBy { it.second.lowercase() } // Alphabetical
        1 -> notes.sortedBy { it.first } // Assuming ID represents creation date
        2 -> notes.sortedByDescending { it.third } // Sort by last updated
        else -> notes
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun DocumentItem(title: String, subtitle: String, id: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("detail/documents/$id") },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//            .clickable { navController.navigate("detail/documents/$id") },
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = Icons.Default.ThumbUp,
//            contentDescription = null,
//            modifier = Modifier.size(24.dp)
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Column {
//            Text(text = title, style = MaterialTheme.typography.bodySmall)
//            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
//        }
//    }
//}

@Composable
fun NoteItem(title: String, subtitle: String, id: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("detail/notes/$id") },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CardDialog(
    onDismiss: () -> Unit,
    onAddSuccess: () -> Unit
) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val categories = listOf("Notes", "Documents")
    var selectedCategory by remember { mutableStateOf("Notes") }
    var expanded by remember { mutableStateOf(false) }
    val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    // Get current user id
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser?.uid
    if (currentUser == null) {
        Toast.makeText(context, "Please sign in first!", Toast.LENGTH_SHORT).show()
        return
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    val firestore = FirebaseFirestore.getInstance()
                    val data = mutableMapOf<String, Any>()

//                    data["category"] = selectedCategory
                    data["title"] = input
                    data["content"] = contentInput
                    data["lastUpdated"] = currentDateTime
                    data["user_id"] = currentUser

                    val collectionName = when (selectedCategory) {
                        "Notes" -> "notes"
                        "Documents" -> "documents"
                        else -> "notes"
                    }

                    firestore.collection(collectionName)
                        .add(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "$selectedCategory added successfully!", Toast.LENGTH_SHORT).show()
                            onAddSuccess()
                            onDismiss()

                        }
                        .addOnFailureListener{e ->
                            Toast.makeText(context, "Error adding $selectedCategory: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
//                modifier = Modifier.testTag("ConfirmButton")
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
//                modifier = Modifier.testTag("CancelButton")
            ) {
                Text("Cancel")
            }
        },
        text = {
            Column {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Write Something...") },
                    modifier = Modifier
                        .fillMaxWidth()
//                        .testTag("textInput")
                )
                TextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("Content goes here...") },
                    modifier = Modifier
                        .fillMaxWidth()
//                        .testTag("contentInput")
                )
//                Text(
//                    text = "Category: $selectedCategory",
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { expanded = true }
//                        .padding(vertical = 8.dp)
//                        .testTag("CategoryDropdown"),
//                    style = MaterialTheme.typography.bodyMedium
//                )
//                DropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false }
//                ) {
//                    categories.forEach { category ->
//                        DropdownMenuItem(
//                            onClick = {
//                                selectedCategory = category
//                                expanded = false
//                            },
//                            text = { Text(category) }
//                        )
//                    }
//                }
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Category: $selectedCategory",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .align(Alignment.Center)
                            .padding(end = 32.dp)
                        //style = MaterialTheme.typography.bodyMedium
                    )
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x= 12.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Category: $selectedCategory"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.5f)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    },
                                    text = { Text(category) }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                        }
                    }
                }
        }
    )
}


