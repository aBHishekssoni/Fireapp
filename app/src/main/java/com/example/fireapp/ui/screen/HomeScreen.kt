package com.example.fireapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: AppState,
    userData: UserData?,
    onEvent: (AppEvent) -> Unit = {},
    onLogoutClicked: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fiery App") },
                actions = {
                    IconButton(onClick = onLogoutClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.message,
                    onValueChange = { onEvent(AppEvent.OnUpdateMessage(it)) },
                    label = { Text("Type your message!") },
                    modifier = Modifier.weight(8f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                when (state.chatSendStatus) {
                    ChatSendStatus.SENDING -> {
                        CircularProgressIndicator()
                    }

                    else -> {
                        FloatingActionButton(
                            onClick = {
                                userData?.let {
                                    onEvent(AppEvent.OnSendEvent(userData))
                                }
                            },
                            modifier = Modifier.weight(2f),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.Send,
                                contentDescription = "send"
                            )
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = modifier.padding(it)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
            ) {
                when (state.chatListStatus) {
                    ChatListStatus.LOADING -> {
                        item {
                            CircularProgressIndicator()
                        }
                    }

                    ChatListStatus.SUCCESS -> {
                        items(state.chatList) { msg ->
                            MassageCard(msg)
                        }
                    }

                    ChatListStatus.ERROR -> {
                        item {
                            Text(text = state.loadingError,
                                color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
//                items(state.chatList) { msg ->
//                    MassageCard(msg)
//                }
            }
        }
    }
}

@Composable
fun MassageCard(chatMessage: ChatMessage) {
    Text(text = chatMessage.message)
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        state = AppState(),
        userData = UserData("John Doe", "John@gmail.com", "https://picsum.photos/200"),
        onLogoutClicked = {}
    )
}