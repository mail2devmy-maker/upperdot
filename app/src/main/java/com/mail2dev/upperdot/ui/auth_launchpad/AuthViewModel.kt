package com.mail2dev.upperdot.ui.auth_launchpad

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.mail2dev.upperdot.data.network.GoogleAuthService
import com.mail2dev.upperdot.data.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authService: GoogleAuthService,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _showGuestWarning = MutableStateFlow(false)
    val showGuestWarning: StateFlow<Boolean> = _showGuestWarning.asStateFlow()

    private val _signInIntent = MutableStateFlow<Intent?>(null)
    val signInIntent: StateFlow<Intent?> = _signInIntent.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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
                syncManager.startImmediateSync()
                onSuccess()
            } else {
                _errorMessage.value = "Sign in failed: Account is null"
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            _errorMessage.value = "Google Sign-In Error: ${e.statusCode}"
        } catch (e: Exception) {
            e.printStackTrace()
            _errorMessage.value = "Unexpected Error: ${e.message}"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun onTryAsGuestClicked() {
        _showGuestWarning.value = true
    }

    fun dismissGuestWarning() {
        _showGuestWarning.value = false
    }

    fun confirmGuestMode(onSuccess: () -> Unit) {
        _showGuestWarning.value = false
        viewModelScope.launch {
            // Initialize local Room DB context locally for guest mode
            onSuccess()
        }
    }
}
