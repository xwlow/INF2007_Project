package com.example.inf2007_project.consultation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inf2007_project.uam.AuthViewModel
import com.example.inf2007_project.notes.CardDialog
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConsultationsPage(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val consultations = remember { mutableStateListOf<Triple<String, String, String>>() }
    var isDialogOpen by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) } // Refresh trigger state

    LaunchedEffect(refreshTrigger) {
        // Get the current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUser != null) {
            // Fetch documents where user_id matches the current user ID
            val consultationsResult = firestore.collection("consultations")
                .whereEqualTo("user_id", currentUser) // Filter by user_id
                .get()
                .await()

            // Clear and populate the documents list
            consultations.clear()
            consultations.addAll(consultationsResult.documents.mapNotNull { consultations ->
                val title = consultations.getString("title")
                val lastUpdated = consultations.getString("lastUpdated")
                val id = consultations.id
                if (title != null && lastUpdated != null) Triple(id, title, lastUpdated) else null
            })

            Log.d("FirestoreDebug", "Documents retrieved: $consultations")
        } else {
            Log.e("FirestoreDebug", "No user is currently logged in!")
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Consultations",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
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
fun ConsultationTabs() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Past", "Upcoming")

    TabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(text = title) }
            )
        }
    }
}

@Composable
fun ConsultationItem(title: String, subtitle: String, id: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("detail/consultations/$id") },
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
