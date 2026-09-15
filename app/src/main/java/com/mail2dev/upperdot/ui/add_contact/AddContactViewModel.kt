package com.mail2dev.upperdot.ui.add_contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.repository.BankSuggestionRepository
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
    val bankName: String = "",
    val holderName: String = "",
    val accountNumber: String = ""
)

data class AddContactUiState(
    val fullName: String = "",
    val nicknames: String = "",
    val phoneNumbers: List<String> = listOf(""),
    val remark: String = "",
    val emails: List<String> = listOf(""),
    val socialProfiles: List<SocialProfile> = listOf(SocialProfile()),
    val groupName: String = "",
    val tagName: String = "",
    val companyName: String = "",
    val businessCategory: String = "Services",
    val officeAddress: String = "",
    val bankAccounts: List<BankAccount> = listOf(BankAccount()),
    val avatarPath: String? = null,
    
    // UI states
    val isIdentityExpanded: Boolean = false,
    val isCorporateExpanded: Boolean = false,
    val isFinancialExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val showDuplicateWarning: Boolean = false,
    val duplicateConflict: ContactEntity? = null,
    val nameError: String? = null
)

sealed class AddContactEvent {
    object SaveSuccess : AddContactEvent()
    data class ValidationError(val message: String) : AddContactEvent()
}

class AddContactViewModel(
    private val repository: ContactRepository,
    private val hierarchyRepository: HierarchyRepository,
    private val bankSuggestionRepository: BankSuggestionRepository
) : ViewModel() {

    private var editingContactId: Long? = null
    val isEditMode: Boolean get() = editingContactId != null
    
    private var originalContactState: ContactEntity? = null

    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddContactEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val availableGroups: StateFlow<List<HierarchyGroup>> = hierarchyRepository.groups

    val savedBanks: StateFlow<List<String>> = bankSuggestionRepository.savedBanks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, nameError = if (value.isNotBlank()) null else it.nameError) }
    }

    fun onNicknamesChange(value: String) {
        _uiState.update { it.copy(nicknames = value) }
    }

    fun onRemarkChange(value: String) {
        _uiState.update { it.copy(remark = value) }
    }

    fun onPhoneNumberChange(index: Int, value: String) {
        val list = _uiState.value.phoneNumbers.toMutableList()
        if (index < list.size) {
            list[index] = value
            _uiState.update { it.copy(phoneNumbers = list) }
        }
    }

    fun addPhoneNumber() {
        _uiState.update { it.copy(phoneNumbers = it.phoneNumbers + "") }
    }

    fun removePhoneNumber(index: Int) {
        val list = _uiState.value.phoneNumbers.toMutableList()
        if (index < list.size && list.size > 1) {
            list.removeAt(index)
            _uiState.update { it.copy(phoneNumbers = list) }
        }
    }

    fun onEmailChange(index: Int, value: String) {
        val list = _uiState.value.emails.toMutableList()
        if (index < list.size) {
            list[index] = value
            _uiState.update { it.copy(emails = list) }
        }
    }

    fun addEmailField() {
        _uiState.update { it.copy(emails = it.emails + "") }
    }

    fun removeEmailField(index: Int) {
        val list = _uiState.value.emails.toMutableList()
        if (index < list.size && list.size > 1) {
            list.removeAt(index)
            _uiState.update { it.copy(emails = list) }
        }
    }

    fun onGroupNameChange(value: String) {
        _uiState.update { it.copy(groupName = value, tagName = "") }
    }

    fun onTagNameChange(value: String) {
        _uiState.update { it.copy(tagName = value) }
    }

    fun onCreateNewGroup(name: String) {
        hierarchyRepository.addGroup(name)
        _uiState.update { it.copy(groupName = name, tagName = "") }
    }

    fun onSocialPlatformChange(index: Int, platform: String) {
        val list = _uiState.value.socialProfiles.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(platform = platform)
            _uiState.update { it.copy(socialProfiles = list) }
        }
    }

    fun onSocialHandleChange(index: Int, handle: String) {
        val list = _uiState.value.socialProfiles.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(handle = handle)
            _uiState.update { it.copy(socialProfiles = list) }
        }
    }

    fun addSocialProfile() {
        _uiState.update { it.copy(socialProfiles = it.socialProfiles + SocialProfile()) }
    }

    fun onCompanyNameChange(value: String) {
        _uiState.update { it.copy(companyName = value) }
    }

    fun onBusinessCategoryChange(value: String) {
        _uiState.update { it.copy(businessCategory = value) }
    }

    fun onOfficeAddressChange(value: String) {
        _uiState.update { it.copy(officeAddress = value) }
    }

    fun onBankNameChange(index: Int, value: String) {
        val list = _uiState.value.bankAccounts.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(bankName = value)
            _uiState.update { it.copy(bankAccounts = list) }
        }
    }

    fun onBankHolderNameChange(index: Int, value: String) {
        val list = _uiState.value.bankAccounts.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(holderName = value)
            _uiState.update { it.copy(bankAccounts = list) }
        }
    }

    fun onBankAccountNumberChange(index: Int, value: String) {
        val list = _uiState.value.bankAccounts.toMutableList()
        if (index < list.size) {
            list[index] = list[index].copy(accountNumber = value)
            _uiState.update { it.copy(bankAccounts = list) }
        }
    }

    fun addBankAccount() {
        _uiState.update { it.copy(bankAccounts = it.bankAccounts + BankAccount()) }
    }

    fun removeBankAccount(index: Int) {
        val list = _uiState.value.bankAccounts.toMutableList()
        if (index < list.size && list.size > 1) {
            list.removeAt(index)
            _uiState.update { it.copy(bankAccounts = list) }
        }
    }

    fun toggleIdentityExpanded() {
        _uiState.update { it.copy(isIdentityExpanded = !it.isIdentityExpanded) }
    }

    fun toggleCorporateExpanded() {
        _uiState.update { it.copy(isCorporateExpanded = !it.isCorporateExpanded) }
    }

    fun toggleFinancialExpanded() {
        _uiState.update { it.copy(isFinancialExpanded = !it.isFinancialExpanded) }
    }

    fun onAvatarChanged(path: String?) {
        _uiState.update { it.copy(avatarPath = path) }
    }

    val hasUnsavedChanges: Boolean get() {
        val state = _uiState.value
        val currentPhoneNumbers = state.phoneNumbers.filter { it.isNotEmpty() }
        val currentEmails = state.emails.filter { it.isNotEmpty() }
        val currentSocial = state.socialProfiles.filter { it.handle.isNotEmpty() }
        val currentBanks = state.bankAccounts.filter { it.accountNumber.isNotEmpty() }
        val currentNicknames = state.nicknames.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (originalContactState == null) {
            return state.fullName.isNotEmpty() ||
                    currentNicknames.isNotEmpty() ||
                    currentPhoneNumbers.isNotEmpty() ||
                    state.remark.isNotEmpty() ||
                    currentEmails.isNotEmpty() ||
                    state.officeAddress.isNotEmpty() ||
                    state.companyName.isNotEmpty()
        }

        return state.fullName != originalContactState!!.fullName ||
                currentNicknames != originalContactState!!.nicknames ||
                currentPhoneNumbers != originalContactState!!.phoneNumbers ||
                state.remark != (originalContactState!!.remark ?: "") ||
                currentEmails != originalContactState!!.emails ||
                state.groupName != originalContactState!!.groupName ||
                state.tagName != (originalContactState!!.tagName ?: "") ||
                state.companyName != (originalContactState!!.companyName ?: "") ||
                state.businessCategory != originalContactState!!.businessCategory ||
                state.officeAddress != (originalContactState!!.physicalAddress ?: "") ||
                currentSocial != originalContactState!!.socialProfiles ||
                currentBanks != originalContactState!!.bankAccounts ||
                state.avatarPath != originalContactState!!.avatarPath
    }

    fun loadContact(id: Long) {
        if (id <= 0L) return
        viewModelScope.launch {
            repository.getContactById(id)?.let { contact ->
                editingContactId = contact.id
                originalContactState = contact
                _uiState.update { it.copy(
                    fullName = contact.fullName,
                    nicknames = contact.nicknames.joinToString(", "),
                    phoneNumbers = contact.phoneNumbers.ifEmpty { listOf("") },
                    remark = contact.remark ?: "",
                    emails = contact.emails.ifEmpty { listOf("") },
                    socialProfiles = contact.socialProfiles.ifEmpty { listOf(SocialProfile()) },
                    groupName = contact.groupName,
                    tagName = contact.tagName ?: "",
                    companyName = contact.companyName ?: "",
                    businessCategory = contact.businessCategory,
                    officeAddress = contact.physicalAddress ?: "",
                    bankAccounts = contact.bankAccounts.ifEmpty { listOf(BankAccount()) },
                    avatarPath = contact.avatarPath,
                    showDiscardDialog = false
                ) }
            }
        }
    }

    fun resetForm() {
        editingContactId = null
        originalContactState = null
        _uiState.value = AddContactUiState()
    }

    fun prefillPhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumbers = listOf(phone)) }
    }

    fun onDiscardRequest(onConfirmImmediately: () -> Unit) {
        if (hasUnsavedChanges) {
            _uiState.update { it.copy(showDiscardDialog = true) }
        } else {
            onConfirmImmediately()
        }
    }

    fun dismissDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    fun dismissDuplicateWarning() {
        _uiState.update { it.copy(showDuplicateWarning = false, duplicateConflict = null) }
    }

    fun saveContact(onSuccess: () -> Unit, forceSave: Boolean = false) {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(nameError = "Full Name is required") }
            viewModelScope.launch {
                _eventFlow.emit(AddContactEvent.ValidationError("Please provide a name for this contact"))
            }
            return
        }

        if (state.isSaving) return

        // Duplicate Check (only for new contacts)
        if (editingContactId == null && !forceSave) {
            viewModelScope.launch(Dispatchers.IO) {
                val existing = repository.allContacts.first().find { ex ->
                    // Match Name exactly
                    val nameMatch = ex.fullName.equals(state.fullName, ignoreCase = true)
                    
                    // Match any Phone
                    val phoneMatch = state.phoneNumbers.any { inPh ->
                        ex.phoneNumbers.any { exPh ->
                            com.mail2dev.upperdot.util.ContactUtils.isSamePhoneNumber(inPh, exPh)
                        }
                    }

                    nameMatch || (phoneMatch && state.phoneNumbers.any { it.isNotBlank() })
                }

                if (existing != null) {
                    _uiState.update { it.copy(showDuplicateWarning = true, duplicateConflict = existing) }
                    return@launch
                }
                
                performSave(onSuccess)
            }
        } else {
            performSave(onSuccess)
        }
    }

    private fun performSave(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, showDuplicateWarning = false) }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val primaryPhone = state.phoneNumbers.firstOrNull() ?: ""
                val sanitized = com.mail2dev.upperdot.util.ContactUtils.smartSanitize(primaryPhone)
                
                val entity = ContactEntity(
                    id = editingContactId ?: 0L,
                    fullName = state.fullName,
                    nicknames = state.nicknames.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    phoneNumbers = state.phoneNumbers.filter { it.isNotEmpty() },
                    sanitizedPrimaryPhone = sanitized,
                    remark = state.remark,
                    emails = state.emails.filter { it.isNotEmpty() },
                    groupName = state.groupName.ifEmpty { "Unassigned" },
                    tagName = state.tagName.ifEmpty { null },
                    socialProfiles = state.socialProfiles.filter { it.handle.isNotEmpty() },
                    companyName = state.companyName,
                    businessCategory = state.businessCategory,
                    physicalAddress = state.officeAddress,
                    bankAccounts = state.bankAccounts.filter { it.accountNumber.isNotEmpty() },
                    avatarPath = state.avatarPath
                )
                
                if (editingContactId != null) {
                    repository.updateContact(entity)
                } else {
                    val newId = repository.insertContact(entity)
                    editingContactId = newId
                }

                state.bankAccounts.forEach { account ->
                    if (account.bankName.isNotBlank()) {
                        bankSuggestionRepository.saveBankName(account.bankName)
                    }
                }

                _eventFlow.emit(AddContactEvent.SaveSuccess)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _eventFlow.emit(AddContactEvent.ValidationError("Failed to save contact: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
