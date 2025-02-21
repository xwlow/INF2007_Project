package com.example.inf2007_project.notes

import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.inf2007_project.pages.BottomNavigationBar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotesRemindersPage(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel){
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val documents = remember { mutableStateListOf<Triple<String, String, String>>() }
    val notes = remember { mutableStateListOf<Triple<String, String, String>>() }
    var isDialogOpen by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) } // Refresh trigger state
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        // Get the current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUser != null) {
            // Fetch documents where user_id matches the current user ID
            val documentsResult = firestore.collection("documents")
                .whereEqualTo("user_id", currentUser) // Filter by user_id
                .get()
                .await()

            // Clear and populate the documents list
            documents.clear()
            documents.addAll(documentsResult.documents.mapNotNull { doc ->
                val title = doc.getString("title")
                val lastUpdated = doc.getString("lastUpdated")
                val id = doc.id
                if (title != null && lastUpdated != null) Triple(id, title, lastUpdated) else null
            })


            //// NOTES ////
            val notesResult = firestore.collection("notes")
                .whereEqualTo("user_id", currentUser) // Filter by user_id
                .get()
                .await()

            // Clear and populate the notes list
            notes.clear()
            notes.addAll(notesResult.documents.mapNotNull { doc ->
                val title = doc.getString("title")
                val lastUpdated = doc.getString("lastUpdated")
                val id = doc.id
                if (title != null && lastUpdated != null) Triple(id, title, lastUpdated) else null
            })

            Log.d("FirestoreDebug", "Documents retrieved: $documents")
        } else {
            Log.e("FirestoreDebug", "No user is currently logged in!")
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
            documents.forEach { (id, title, lastUpdated) ->
                DocumentItem(title, "Last updated on $lastUpdated",id, navController = navController)
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader("Notes")
            NotesTabs(selectedTabIndex) { newIndex -> selectedTabIndex = newIndex }
            val sortedNotes = getSortedNotes(notes, selectedTabIndex)

            sortedNotes.forEach { (id, title, lastUpdated) ->
                NoteItem(title, "Last updated on $lastUpdated", id, navController)
            }

            if (isDialogOpen) {
                CardDialog(
                    onDismiss = { isDialogOpen = false },
                    onAddSuccess = { refreshTrigger++ }
                )
            }
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
                imageVector = Icons.Default.ThumbUp,
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
                imageVector = Icons.Default.ThumbUp,
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
                modifier = Modifier.testTag("ConfirmButton")
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.testTag("CancelButton")
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
                        .testTag("textInput")
                )
                TextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("Content goes here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contentInput")
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
                                .align(Alignment.Center)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Category: $selectedCategory"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
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


