package com.example.inf2007_project.pages

//import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.AuthState
import com.example.inf2007_project.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.example.inf2007_project.testData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun Messaging(modifier: Modifier = Modifier, navController : NavController, authViewModel: AuthViewModel, testViewModel: TestViewModel){


    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()
    val recipientId = RecipientHolder.recipientId

    // Fetch messages from Firestore


//    LaunchedEffect((authState.value)) {
//        when(authState.value){
//            is AuthState.Unauthenticated -> {
//                navController.navigate("login")
//                Toast.makeText(context, "You have successfully signed out!", Toast.LENGTH_SHORT).show()
//            }
//            else -> Unit
    LaunchedEffect(Unit) {
        if (recipientId != null) {
            Log.d("recipientId", recipientId)
            currentUser?.uid?.let { Log.d("senderId", it) }
        }
        val query1 = db.collection("messages")
            .whereEqualTo("senderId", currentUser?.uid
            )
            .whereEqualTo("recipientId", recipientId)
            .orderBy("timestamp")

        val query2 = db.collection("messages")
            .whereEqualTo("senderId", recipientId)
            .whereEqualTo("recipientId", currentUser?.uid
            )
            .orderBy("timestamp")

        query1.addSnapshotListener { snapshot1, e1 ->
            query2.addSnapshotListener { snapshot2, e2 ->
                if (e1 == null && e2 == null && snapshot1 != null && snapshot2 != null) {
                    val messages1 = snapshot1.documents.map {
                        Message(
                            message = it.getString("message") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            recipientId = it.getString("recipientId") ?: "",
                            timestamp = it.getTimestamp("timestamp")?.toDate().toString()
                        )
                    }

                    val messages2 = snapshot2.documents.map {
                        Message(
                            message = it.getString("message") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            recipientId = it.getString("recipientId") ?: "",
                            timestamp = it.getTimestamp("timestamp")?.toDate().toString()
                        )
                    }

                    // Combine both query results and sort by timestamp
                    messages = (messages1 + messages2).sortedBy { it.timestamp }
                    for (message in messages) {
                        Log.d("Messages", "Message: ${message.message}, Sender: ${message.senderId}, Recipient: ${message.recipientId} ,Timestamp: ${message.timestamp}")
                    }

                }
            }
        }
    }
//        }
//    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Padding to avoid overlapping with the bottom bar
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(messages.size) { index ->
                    MessageItem(
                        message = messages[index],
                        isCurrentUser = messages[index].senderId == currentUser?.uid
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.LightGray, CircleShape)
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (messageText.text.isNotBlank()) {
                            scope.launch {
                                val messageData = mapOf(
                                    "message" to messageText.text,
                                    "senderId" to currentUser?.uid,
                                    "recipientId" to recipientId,
                                    "timestamp" to com.google.firebase.Timestamp.now()
                                )
                                db.collection("messages").add(messageData)
                                messageText = TextFieldValue("")
                                Log.d("Message Sent", "Message has been sent")
                            }
                        }
                    }
                ) {
                    Text(text = "Send")
                }
            }


        }
    }
}

@Composable
fun MessageItem(message: Message, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp,
            color = backgroundColor
        ) {
            Text(
                text = "${message.message}\n${message.timestamp}",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

}

data class Message(
    val message: String,
    val senderId: String,
    val recipientId: String,
    val timestamp: String
)
