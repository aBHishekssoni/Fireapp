package com.example.fireapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import com.example.fireapp.ui.AppNavigation
import com.example.fireapp.ui.screen.AppViewModel
import com.example.fireapp.ui.screen.GoogleAuthUiClient
import com.example.fireapp.ui.screen.HomeScreen
import com.example.fireapp.ui.screen.LoginScreen
import com.example.fireapp.ui.screen.LoginStatus
import com.example.fireapp.ui.theme.FireappTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val googleSingInClient = GoogleAuthUiClient(
            context = this,
            oneTapClient = Identity.getSignInClient(this)
        )
        enableEdgeToEdge()
        setContent {
            FireappTheme {
                val vm = viewModel<AppViewModel>()
                val state by vm.appState.collectAsStateWithLifecycle()
                val nc = rememberNavController()
                val context = LocalContext.current
                NavHost(navController = nc, startDestination = com.example.fireapp.Screen.Login.route) {
                    composable(com.example.fireapp.Screen.Login.route) {
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartIntentSenderForResult()
                        ) { result ->
                            if (result.resultCode == RESULT_OK) {
                                lifecycleScope.launch {
                                    val signInResult = googleSingInClient.signInWithIntent(
                                        intent = result.data ?: return@launch // if there is no result, just return
                                    )
                                    vm.onSignInResult(signInResult)
                                }
                            }
                        }

                        LaunchedEffect(key1 = state.loginStatus) {
                            when (state.loginStatus) {
                                LoginStatus.LOGGED_IN -> {
                                    Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show()
                                    nc.navigate(com.example.fireapp.Screen.Home.route)
                                }
                                LoginStatus.LOGGED_OUT -> {
                                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                }
                                LoginStatus.IN_PROGRESS -> {
                                    Toast.makeText(context, "In progress", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                LoginStatus.FAILED->{
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                        LoginScreen(
                            modifier = Modifier,
                            state = state
                        ) {
                            vm.setSignInInProgress()
                            lifecycleScope.launch {
                                val signInIntentSender = googleSingInClient.signIn()
                                launcher.launch(
                                    IntentSenderRequest.Builder(
                                        signInIntentSender ?: return@launch
                                    ).build()
                                )
                            }
                        }
                    }
                    composable(com.example.fireapp.Screen.Home.route) {
                        HomeScreen(
                            modifier = Modifier,
                            state = state,
                            userData = googleSingInClient.getSignedInUser(),
                            onEvent = vm::onChatEvent,
                        ){
                            lifecycleScope.launch {
                                googleSingInClient.signOut()
                                vm.resetState()
                                nc.navigate(com.example.fireapp.Screen.Login.route)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class Screen(val route: String) {
    Home("home"),
    Login("login"),
}