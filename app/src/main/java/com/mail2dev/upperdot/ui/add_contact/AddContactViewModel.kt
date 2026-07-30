package com.mail2dev.upperdot.ui.add_contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.data.repository.HierarchyRepository
import com.mail2dev.upperdot.ui.relationship_hierarchy.HierarchyGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class SocialProfile(
    val platform: String = "WhatsApp",
    val handle: String = ""
)

@Serializable
data class BankAccount(
    val bankName: String = "Maybank",
    val holderName: String = "",
    val accountNumber: String = ""
)

class AddContactViewModel(
    private val repository: ContactRepository,
    private val hierarchyRepository: HierarchyRepository
) : ViewModel() {

    // Step 1: Core Info
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _nicknames = MutableStateFlow("")
    val nicknames: StateFlow<String> = _nicknames.asStateFlow()

    private val _phoneNumbers = MutableStateFlow(listOf(""))
    val phoneNumbers: StateFlow<List<String>> = _phoneNumbers.asStateFlow()

    // Step 2: Identity
    private val _emails = MutableStateFlow(listOf(""))
    val emails: StateFlow<List<String>> = _emails.asStateFlow()

    private val _socialProfiles = MutableStateFlow(listOf(SocialProfile()))
    val socialProfiles: StateFlow<List<SocialProfile>> = _socialProfiles.asStateFlow()

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName.asStateFlow()

    private val _tagName = MutableStateFlow("")
    val tagName: StateFlow<String> = _tagName.asStateFlow()

    val availableGroups: StateFlow<List<HierarchyGroup>> = hierarchyRepository.groups

    // Step 3: Corporate
    private val _companyName = MutableStateFlow("")
    val companyName: StateFlow<String> = _companyName.asStateFlow()

    private val _businessCategory = MutableStateFlow("Services")
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

    fun removePhoneNumber(index: Int) {
        val list = _phoneNumbers.value.toMutableList()
        if (index < list.size && list.size > 1) {
            list.removeAt(index)
            _phoneNumbers.value = list
        }
    }

    // Identity Updates
    fun onEmailChange(index: Int, value: String) {
        val list = _emails.value.toMutableList()
        if (index < list.size) {
            list[index] = value
            _emails.value = list
        }
    }

    fun addEmailField() {
        _emails.value = _emails.value + ""
    }

    fun removeEmailField(index: Int) {
        val list = _emails.value.toMutableList()
        if (index < list.size && list.size > 1) {
            list.removeAt(index)
            _emails.value = list
        }
    }
    fun onGroupNameChange(value: String) { 
        _groupName.value = value 
        _tagName.value = "" // Reset tag when group changes
    }
    fun onTagNameChange(value: String) { _tagName.value = value }

    fun onCreateNewGroup(name: String) {
        hierarchyRepository.addGroup(name)
        _groupName.value = name
        _tagName.value = ""
    }
    
    fun onSocialPlatformChange(index: Int, platform: String) {
        val list = _socialProfiles.value.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(platform = platform)
            _socialProfiles.value = list
        }
    }

    fun onSocialHandleChange(index: Int, handle: String) {
        val list = _socialProfiles.value.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(handle = handle)
            _socialProfiles.value = list
        }
    }

    fun addSocialProfile() {
        _socialProfiles.value = _socialProfiles.value + SocialProfile()
    }

    // Corporate Updates
    fun onCompanyNameChange(value: String) { _companyName.value = value }
    fun onBusinessCategoryChange(value: String) { _businessCategory.value = value }
    fun onOfficeAddressChange(value: String) { _officeAddress.value = value }

    // Financial Updates
    fun onBankNameChange(index: Int, value: String) {
        val list = _bankAccounts.value.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(bankName = value)
            _bankAccounts.value = list
        }
    }

    fun onBankHolderNameChange(index: Int, value: String) {
        val list = _bankAccounts.value.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(holderName = value)
            _bankAccounts.value = list
        }
    }

    fun onBankAccountNumberChange(index: Int, value: String) {
        val list = _bankAccounts.value.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(accountNumber = value)
            _bankAccounts.value = list
        }
    }

    fun addBankAccount() {
        _bankAccounts.value = _bankAccounts.value + BankAccount()
    }

    fun removeBankAccount(index: Int) {
        val list = _bankAccounts.value.toMutableList()
        if (index < list.size && list.size > 1) {
            list.removeAt(index)
            _bankAccounts.value = list
        }
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
        
        viewModelScope.launch(Dispatchers.IO) {
            val entity = ContactEntity(
                id = 0L,
                fullName = _fullName.value,
                nicknames = _nicknames.value.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                phoneNumbers = _phoneNumbers.value.filter { it.isNotEmpty() },
                sanitizedPrimaryPhone = _phoneNumbers.value.firstOrNull()?.replace(Regex("[^0-9]"), "") ?: "",
                emails = _emails.value.filter { it.isNotEmpty() },
                groupName = _groupName.value.ifEmpty { "Unassigned" },
                tagName = _tagName.value.ifEmpty { null },
                socialProfiles = _socialProfiles.value.filter { it.handle.isNotEmpty() },
                companyName = _companyName.value,
                businessCategory = _businessCategory.value,
                physicalAddress = _officeAddress.value,
                bankAccounts = _bankAccounts.value.filter { it.accountNumber.isNotEmpty() }
            )
            repository.insertContact(entity)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
