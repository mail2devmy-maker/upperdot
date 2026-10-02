package com.mail2dev.upperdot.util

import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.model.SearchMatch

object SearchUtils {

    fun extractSearchMatches(
        contact: ContactEntity,
        query: String,
        groupName: String? = null,
        tagName: String? = null
    ): List<SearchMatch> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return emptyList()

        val matches = mutableListOf<SearchMatch>()

        // 1. Remarks / Notes
        contact.remark?.let { remarkText ->
            checkField("REMARK / NOTE", remarkText, cleanQuery)?.let { matches.add(it) }
        }

        // 2. Business Info
        val businessParts = listOfNotNull(
            contact.companyName?.takeIf { it.isNotBlank() },
            contact.businessCategory.takeIf { it.isNotBlank() && it != "General" }
        )
        if (businessParts.isNotEmpty()) {
            checkField("BUSINESS INFO", businessParts.joinToString(" • "), cleanQuery)?.let { matches.add(it) }
        }

        // 3. Social Profiles
        if (contact.socialProfiles.isNotEmpty()) {
            val socialText = contact.socialProfiles.joinToString(" | ") { "${it.platform}: ${it.handle}" }
            checkField("SOCIAL LINK", socialText, cleanQuery)?.let { matches.add(it) }
        }

        // 4. Address
        contact.physicalAddress?.takeIf { it.isNotBlank() }?.let { address ->
            checkField("ADDRESS", address, cleanQuery)?.let { matches.add(it) }
        }

        // 5. Bank Accounts
        if (contact.bankAccounts.isNotEmpty()) {
            val bankText = contact.bankAccounts.joinToString(" | ") { bank ->
                listOfNotNull<String>(
                    bank.bankName.takeIf { it.isNotBlank() },
                    bank.accountNumber.takeIf { it.isNotBlank() },
                    bank.holderName.takeIf { it.isNotBlank() }
                ).joinToString(" - ")
            }
            checkField("BANK INFO", bankText, cleanQuery)?.let { matches.add(it) }
        }

        // 6. Name
        checkField("NAME", contact.fullName, cleanQuery)?.let { matches.add(it) }

        // 7. Nicknames
        if (contact.nicknames.isNotEmpty()) {
            val nicknamesText = contact.nicknames.joinToString(", ")
            checkField("NICKNAME", nicknamesText, cleanQuery)?.let { matches.add(it) }
        }

        // 8. Emails
        if (contact.emails.isNotEmpty()) {
            val emailText = contact.emails.joinToString(", ")
            checkField("EMAIL", emailText, cleanQuery)?.let { matches.add(it) }
        }

        // 9. Phone Numbers
        if (contact.phoneNumbers.isNotEmpty()) {
            val phoneText = contact.phoneNumbers.joinToString(", ")
            checkField("PHONE", phoneText, cleanQuery)?.let { matches.add(it) }
        }

        // 10. Tag / Group
        val tagGroupParts = listOfNotNull(
            tagName?.takeIf { it.isNotBlank() },
            groupName?.takeIf { it.isNotBlank() && it != "Unassigned" && it != "All" }
        )
        if (tagGroupParts.isNotEmpty()) {
            checkField("GROUP / TAG", tagGroupParts.joinToString(" • "), cleanQuery)?.let { matches.add(it) }
        }

        return matches
    }

    private fun checkField(fieldName: String, text: String, query: String): SearchMatch? {
        if (text.isBlank()) return null
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        val firstIdx = lowerText.indexOf(lowerQuery)
        if (firstIdx == -1) return null

        val windowBefore = 30
        val windowAfter = 30
        val start = maxOf(0, firstIdx - windowBefore)
        val end = minOf(text.length, firstIdx + query.length + windowAfter)

        val prefix = if (start > 0) "..." else ""
        val suffix = if (end < text.length) "..." else ""
        val snippetText = prefix + text.substring(start, end) + suffix

        val matchRanges = mutableListOf<IntRange>()
        var searchIndex = 0
        val lowerSnippet = snippetText.lowercase()
        while (searchIndex < lowerSnippet.length) {
            val foundIdx = lowerSnippet.indexOf(lowerQuery, searchIndex)
            if (foundIdx == -1) break
            matchRanges.add(foundIdx until (foundIdx + query.length))
            searchIndex = foundIdx + lowerQuery.length
        }

        return SearchMatch(
            fieldName = fieldName,
            snippet = snippetText,
            matchRanges = matchRanges
        )
    }
}
