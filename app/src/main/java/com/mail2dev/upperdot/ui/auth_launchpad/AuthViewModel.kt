package com.mail2dev.upperdot.ui.auth_launchpad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _showGuestWarning = MutableStateFlow(false)
    val showGuestWarning: StateFlow<Boolean> = _showGuestWarning.asStateFlow()

    fun onSignInWithGoogleClicked() {
        // TODO: Implement Google Sign-In intent flow
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
