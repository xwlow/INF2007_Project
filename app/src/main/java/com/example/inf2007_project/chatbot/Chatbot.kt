package com.example.inf2007_project.chatbot

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.inf2007_project.pages.BottomNavigationBar
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun ChatBotScreen(modifier: Modifier = Modifier, navController: NavController) {
    var chatMessages by remember { mutableStateOf(listOf<String>()) }
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)
                // Apply scaffold's bottom padding to avoid navigation bar overlap
                .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            // Add top safe area padding to avoid camera hole punch
            //Spacer(modifier = Modifier.height(48.dp))

            // Header with title - moved down with padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                    contentDescription = "Health Bot Icon",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Clarify your doubts with Health Bot",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Chat History in a card with proper weight
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                        //.testTag("chatHistoryList"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages) { message ->
                        val isUserMessage = message.startsWith("You:")
                        val backgroundColor =
                            if (isUserMessage) Color(0xFF2196F3) else Color(0xFFE0E0E0)
                        val textColor = if (isUserMessage) Color.White else Color.Black

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .background(
                                        color = backgroundColor,
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUserMessage) 12.dp else 0.dp,
                                            bottomEnd = if (isUserMessage) 0.dp else 12.dp
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = message.removePrefix("You: ").removePrefix("AI 🤖: "),
                                    fontSize = 16.sp,
                                    color = textColor,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input area with improved styling - matching the screenshot style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 1
                )

                Button(
                    onClick = {
                        val inputText = userInput.text.trim()
                        if (inputText.isNotBlank()) {
                            val userMessage = "You: $inputText"
                            chatMessages = chatMessages + userMessage
                            userInput = TextFieldValue("")

                            coroutineScope.launch {
                                val aiResponse = getAIResponseSync(inputText)
                                chatMessages = chatMessages + "AI 🤖: $aiResponse"
                            }
                        } else {
                            Toast.makeText(context, "Enter a message!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .width(84.dp)
                ) {
                    Text("Send")
                }
            }
        }
    }
}

//AIzaSyB29zHV1-Muk5o5_M_UGajLAv1krEufxkY --> API Key
// Function to Call Gemini AI API

fun getAIResponseSync(prompt: String): String {
    val apiKey = "AIzaSyB29zHV1-Muk5o5_M_UGajLAv1krEufxkY"  // Replace with your actual Gemini API key

    val context = """
        Provide feedback to general medical enquiries such as medication information such as daily intake and side effects, shorten the text but still inform user to consult a doctor and not trust everything you generate
    """.trimIndent()

    // Combine the context and user query to form the prompt
    val combinedPrompt = "$context\n\n$prompt"

    return try {

        // Corrected model name
        val model = GenerativeModel(
            modelName = "models/gemini-1.5-flash",  // Ensure correct model name
            apiKey = apiKey
        )
        runBlocking {
            val response = model.generateContent(content { text(combinedPrompt) })
            response.text ?: "I'm not sure how to respond to that!"
        }
    } catch (e: Exception) {
        Log.e("GeminiAPI", "Error fetching AI response", e)
        "Oops! Something went wrong: ${e.message}"
    }

}
