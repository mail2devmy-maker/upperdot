package com.mail2dev.upperdot.ui.auth_launchpad

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.mail2dev.upperdot.data.network.GoogleAuthService
import com.mail2dev.upperdot.data.repository.PreferenceRepository
import com.mail2dev.upperdot.data.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel(
    application: Application,
    private val authService: GoogleAuthService,
    private val syncManager: SyncManager,
    private val preferenceRepository: PreferenceRepository
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _signInIntent = MutableStateFlow<Intent?>(null)
    val signInIntent: StateFlow<Intent?> = _signInIntent.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val account = authService.getLastSignedInAccount(getApplication())
            if (account != null) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun onSignInWithGoogleClicked() {
        _signInIntent.value = authService.getSignInIntent()
    }

    fun consumeSignInIntent() {
        _signInIntent.value = null
    }

    fun handleSignInResult(data: Intent?, onSuccess: () -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account != null) {
                // Success: Trigger sync and navigate
                viewModelScope.launch {
                    val prefs = preferenceRepository.preferences.first()
                    syncManager.startImmediateSync(wifiOnly = prefs.syncOverWifi)
                }
                _authState.value = AuthState.Authenticated
                onSuccess()
            } else {
                _errorMessage.value = "Sign in failed: Account is null"
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            _errorMessage.value = "Google Sign-In Error: ${e.statusCode}"
            _authState.value = AuthState.Unauthenticated
        } catch (e: Exception) {
            e.printStackTrace()
            _errorMessage.value = "Unexpected Error: ${e.message}"
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}