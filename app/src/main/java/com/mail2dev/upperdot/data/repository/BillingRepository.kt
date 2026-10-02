package com.mail2dev.upperdot.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingRepository(private val context: Context) {

    // Toggle this to test Free vs Premium tier enforcements locally
    companion object {
        var MOCK_PREMIUM_MODE = false
    }

    private val _isPremium = MutableStateFlow(MOCK_PREMIUM_MODE)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        // Initialize RevenueCat Purchases SDK here when API keys are available:
        // Purchases.configure(PurchasesConfiguration.Builder(context, "YOUR_REVENUECAT_API_KEY").build())
        
        // For production readiness, entitlement listener can be attached to update _isPremium flow.
        refreshEntitlements()
    }

    fun refreshEntitlements() {
        // Check actual RevenueCat entitlements if configured, otherwise fallback to mock flag
        _isPremium.value = MOCK_PREMIUM_MODE
    }

    fun setMockPremium(enabled: Boolean) {
        MOCK_PREMIUM_MODE = enabled
        _isPremium.value = enabled
    }
}
