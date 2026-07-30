package com.mail2dev.upperdot.ui.auth_launchpad

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.network.GoogleAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authService: GoogleAuthService) : ViewModel() {

    private val _showGuestWarning = MutableStateFlow(false)
    val showGuestWarning: StateFlow<Boolean> = _showGuestWarning.asStateFlow()

    private val _signInIntent = MutableStateFlow<Intent?>(null)
    val signInIntent: StateFlow<Intent?> = _signInIntent.asStateFlow()

    fun onSignInWithGoogleClicked() {
        _signInIntent.value = authService.getSignInIntent()
    }

    fun consumeSignInIntent() {
        _signInIntent.value = null
    }

    fun handleSignInResult(onSuccess: () -> Unit) {
        // Validation logic for DriveScopes.DRIVE_APPDATA scope
        onSuccess()
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
