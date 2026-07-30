package com.mail2dev.upperdot.ui.add_contact

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SocialProfile(
    val platform: String = "WhatsApp",
    val handle: String = ""
)

data class BankAccount(
    val bankName: String = "Maybank",
    val holderName: String = "",
    val accountNumber: String = ""
)

class AddContactViewModel : ViewModel() {

    // Step 1: Core Info
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _nicknames = MutableStateFlow("")
    val nicknames: StateFlow<String> = _nicknames.asStateFlow()

    private val _phoneNumbers = MutableStateFlow(listOf(""))
    val phoneNumbers: StateFlow<List<String>> = _phoneNumbers.asStateFlow()

    // Step 2: Identity
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _socialProfiles = MutableStateFlow(listOf(SocialProfile()))
    val socialProfiles: StateFlow<List<SocialProfile>> = _socialProfiles.asStateFlow()

    private val _subTag = MutableStateFlow("")
    val subTag: StateFlow<String> = _subTag.asStateFlow()

    // Step 3: Corporate
    private val _companyName = MutableStateFlow("")
    val companyName: StateFlow<String> = _companyName.asStateFlow()

    private val _businessCategory = MutableStateFlow("General")
    val businessCategory: StateFlow<String> = _businessCategory.asStateFlow()

    private val _officeAddress = MutableStateFlow("")
    val officeAddress: StateFlow<String> = _officeAddress.asStateFlow()

    // Step 4: Financial
    private val _bankAccounts = MutableStateFlow(listOf(BankAccount()))
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    // UI State
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _showDiscardDialog = MutableStateFlow(false)
    val showDiscardDialog: StateFlow<Boolean> = _showDiscardDialog.asStateFlow()

    // Update methods
    fun onFullNameChange(value: String) { _fullName.value = value }
    fun onNicknamesChange(value: String) { _nicknames.value = value }
    
    fun onPhoneNumberChange(index: Int, value: String) {
        val list = _phoneNumbers.value.toMutableList()
        if (index < list.size) {
            list[index] = value
            _phoneNumbers.value = list
        }
    }

    fun addPhoneNumber() {
        _phoneNumbers.value = _phoneNumbers.value + ""
    }

    fun onStepSelected(step: Int) {
        _currentStep.value = step
    }

    fun onDiscardRequest() {
        _showDiscardDialog.value = true
    }

    fun dismissDiscardDialog() {
        _showDiscardDialog.value = false
    }

    fun saveContact(onSuccess: () -> Unit) {
        if (_fullName.value.isBlank()) {
            _currentStep.value = 0
            // TODO: Show error state for full name
            return
        }
        // TODO: Persist to Room
        onSuccess()
    }
}
