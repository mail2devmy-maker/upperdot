package com.mail2dev.upperdot.data.repository.telephony

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import com.mail2dev.upperdot.data.repository.ContactRepository
import com.mail2dev.upperdot.ui.call_history.CallLogEntry
import com.mail2dev.upperdot.util.ContactUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallLogRepository(
    private val context: Context,
    private val contactRepository: ContactRepository
) {

    suspend fun getCallLogs(): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntry>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE
        )

        try {
            // 1. Pre-fetch all local contacts for fast lookup
            // This is much faster than doing 50 separate Room queries
            val allLocalContacts = contactRepository.getAllContactsList()
            val contactLookup = mutableMapOf<String, com.mail2dev.upperdot.data.local.entity.ContactEntity>()
            allLocalContacts.forEach { contact ->
                contactLookup[contact.sanitizedPrimaryPhone] = contact
                // Also index additional numbers if any
                contact.phoneNumbers.forEach {
                    val s = ContactUtils.smartSanitize(it)
                    if (s.isNotEmpty()) contactLookup[s] = contact
                }
            }

            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC LIMIT 50"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(CallLog.Calls._ID)
                val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)

                while (it.moveToNext()) {
                    val rawNumber = it.getString(numIdx) ?: ""
                    val sanitized = ContactUtils.smartSanitize(rawNumber)
                    var contactId: Long? = null
                    var displayName: String? = null
                    
                    // 1. Try resolving via pre-fetched Local DB map
                    val localContact = contactLookup[sanitized]
                    var avatarPath: String? = null
                    if (localContact != null) {
                        contactId = localContact.id
                        displayName = localContact.fullName
                        avatarPath = localContact.avatarPath
                    }

                    // 2. Try resolving via System Contacts if not in UpperDot
                    if (displayName.isNullOrBlank()) {
                        displayName = resolveSystemContactName(rawNumber)
                    }

                    list.add(
                        CallLogEntry(
                            id = it.getString(idIdx),
                            contactId = contactId,
                            name = displayName,
                            avatarPath = avatarPath,
                            number = ContactUtils.formatForDisplay(rawNumber),
                            timestamp = it.getLong(dateIdx),
                            type = it.getInt(typeIdx)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    private fun resolveSystemContactName(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
