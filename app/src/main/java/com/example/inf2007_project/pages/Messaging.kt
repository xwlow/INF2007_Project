package com.example.inf2007_project.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.AuthViewModel
import com.example.inf2007_project.TestViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@Composable
fun Messaging(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    testViewModel: TestViewModel
) {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()
    val recipientId = RecipientHolder.recipientId // Assume this holds the recipient's ID.

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (recipientId != null) {
            Log.d("RecipientID", recipientId)
            currentUser?.uid?.let { Log.d("SenderID", it) }

            val query1 = db.collection("messages")
                .whereEqualTo("senderId", currentUser?.uid)
                .whereEqualTo("recipientId", recipientId)
                .orderBy("timestamp")

            val query2 = db.collection("messages")
                .whereEqualTo("senderId", recipientId)
                .whereEqualTo("recipientId", currentUser?.uid)
                .orderBy("timestamp")

            query1.addSnapshotListener { snapshot1, e1 ->
                query2.addSnapshotListener { snapshot2, e2 ->
                    if (e1 == null && e2 == null && snapshot1 != null && snapshot2 != null) {
                        val messages1 = snapshot1.documents.mapNotNull { doc ->
                            Message(
                                message = doc.getString("message") ?: "",
                                senderId = doc.getString("senderId") ?: "",
                                recipientId = doc.getString("recipientId") ?: "",
                                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                            )
                        }

                        val messages2 = snapshot2.documents.mapNotNull { doc ->
                            Message(
                                message = doc.getString("message") ?: "",
                                senderId = doc.getString("senderId") ?: "",
                                recipientId = doc.getString("recipientId") ?: "",
                                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                            )
                        }

                        // Combine and sort by timestamp
                        messages = (messages1 + messages2).sortedBy { it.timestamp.toDate().time }
                        messages.forEach {
                            Log.d("MessageLog", "Message: ${it.message}, Timestamp: ${it.timestamp.toDate()}")
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(messages) {
//        Log.d("Autoscroll", "Messages updated: ${messages.size}")
        if (messages.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messages.size - 1)
                Log.d("Autoscroll", "Scrolled to last item")
            } catch (e: Exception) {
                Log.e("Autoscroll", "Scrolling failed", e)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                state = listState,
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
                    .padding(8.dp),
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
                                    "timestamp" to Timestamp.now()
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
                text = "${message.message}\n${message.timestamp.toDate()}",
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
    val timestamp: Timestamp
)
