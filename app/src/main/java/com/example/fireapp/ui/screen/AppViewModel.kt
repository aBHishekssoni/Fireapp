package com.example.fireapp.ui.screen

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fireapp.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
enum class LoginStatus {
    LOGGED_IN,
    IN_PROGRESS,
    LOGGED_OUT,
    FAILED,
}

enum class ChatListStatus {
    LOADING,
    SUCCESS,
    ERROR,
}

enum class ChatSendStatus {
    EMPTY,
    SENDING,
    SUCCESS,
    ERROR,
}

data class ChatMessage(
    val message: String="",
    val userData: UserData=UserData("","",""),
    val timestamp: Long = System.currentTimeMillis(),
)

data class AppState(
    val loginStatus: LoginStatus = LoginStatus.LOGGED_OUT,
    val errorMessage: String = "",
    val chatList: List<ChatMessage> = emptyList(),
    val message: String = "",
    val sendingError: String = "",
    val loadingError: String = "",
    val chatListStatus: ChatListStatus = ChatListStatus.LOADING,
    val chatSendStatus: ChatSendStatus = ChatSendStatus.EMPTY,
)

class AppViewModel() : ViewModel() {
    private val _appState = MutableStateFlow(AppState())
    val appState = _appState.asStateFlow()

    fun onSignInResult(signInResult: SignInResult) {
        if (signInResult.error != null) {
            _appState.value = AppState(
                loginStatus = LoginStatus.FAILED,
                errorMessage = signInResult.error.message ?: "Unknown error"
            )
        } else {
            _appState.value = AppState(
                loginStatus = LoginStatus.LOGGED_IN,
                errorMessage = ""
            )
        }
    }

    fun resetState() {
        _appState.value = AppState()
    }

    fun setSignInInProgress() {
        _appState.value = AppState(
            loginStatus = LoginStatus.IN_PROGRESS,
            errorMessage = ""
        )
    }

    private val db = Firebase.firestore
    init {
        updateChatList()
    }


    private fun updateChatList() {
        val state = _appState.value
        _appState.update { it.copy(chatListStatus = ChatListStatus.LOADING) }
        viewModelScope.launch {
        try {
            val  result = db.collection("chat").get().await()
            val chatList = result.documents.mapNotNull {snapshot ->
                try {
                    snapshot.toObject(ChatMessage::class.java)
                }
                catch (e: Exception){
                    e.printStackTrace()
                    null
                }
            }
            _appState.update {
                it.copy(
                    chatListStatus = ChatListStatus.SUCCESS,
                    chatList = chatList
                )
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            _appState.update {
                it.copy(
                    chatListStatus = ChatListStatus.ERROR,
                    loadingError = "⚠️ ${e.message ?: "Unknown error"}"
                )
            }
        }
            updateChatList()

        }
    }

    private fun sendMessage(userData: UserData) {
        val state = _appState.value
        if (state.message.isEmpty()) return
        _appState.update { it.copy(chatSendStatus = ChatSendStatus.SENDING) }
        val chatMessage = ChatMessage(
            message = state.message,
            userData = userData,
            timestamp = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            try {
                val reference = db.collection("chat").add(chatMessage).await()
                if (reference.id.isNotEmpty()) {
                    _appState.update {
                        it.copy(
                            chatSendStatus = ChatSendStatus.SUCCESS,
                            message = "",
                            sendingError = "",
                        )
                    }
                } else {
                    _appState.update {
                        it.copy(
                            chatSendStatus = ChatSendStatus.ERROR,
                            sendingError = "⚠️ Unknown error"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _appState.update {
                    it.copy(
                        chatSendStatus = ChatSendStatus.ERROR,
                        sendingError = "⚠️ ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun onChatEvent(event: AppEvent){
        when (event){
            AppEvent.OnRefreshChatList -> {}
            is AppEvent.OnSendEvent -> {
                sendMessage(event.userData)
            }
            is AppEvent.OnUpdateMessage -> {
                _appState.update { it.copy(message = event.message) }
            }
        }
    }
}

sealed class AppEvent {
    data class OnSendEvent(val userData: UserData) : AppEvent()
    data class OnUpdateMessage(val message: String) : AppEvent()
    data object OnRefreshChatList : AppEvent()
}

data class SignInResult(
    val data: UserData?,
    val error: Exception?,
)

data class UserData(
    val uid: String ="",
    val email: String="",
    val photoUrl: String="",
)

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                BeginSignInRequest.Builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .build()
                    )
                    .setAutoSelectEnabled(true)
                    .build()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.let {
                    UserData(
                        uid = it.uid,
                        email = user.email?:"",
                        photoUrl = user.photoUrl.toString()
                    )
                },
                error = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                error = e
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? {
        val user = auth.currentUser
        return user?.let {
            UserData(
                uid = it.uid,
                email = user.email?: "",
                photoUrl = user.photoUrl.toString()
            )
        }
    }
}