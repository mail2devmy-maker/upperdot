package com.mail2dev.upperdot.util

import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.ui.add_contact.BankAccount
import com.mail2dev.upperdot.ui.add_contact.SocialProfile
import org.junit.Assert.*
import org.junit.Test

class SearchUtilsTest {

    @Test
    fun extractSearchMatches_remarkMatch() {
        val contact = ContactEntity(
            id = 1,
            fullName = "(FB) Chris Avocado",
            nicknames = emptyList(),
            phoneNumbers = listOf("0123456789"),
            sanitizedPrimaryPhone = "123456789",
            emails = emptyList(),
            socialProfiles = emptyList(),
            bankAccounts = emptyList(),
            remark = "seller avocado pasir mas"
        )

        val matches = SearchUtils.extractSearchMatches(contact, "mas")
        assertFalse(matches.isEmpty())

        val remarkMatch = matches.find { it.fieldName == "REMARK / NOTE" }
        assertNotNull(remarkMatch)
        assertEquals("seller avocado pasir mas", remarkMatch!!.snippet)
        assertEquals(1, remarkMatch.matchRanges.size)

        val range = remarkMatch.matchRanges.first()
        val highlighted = remarkMatch.snippet.substring(range.first, range.last + 1)
        assertEquals("mas", highlighted)
    }

    @Test
    fun extractSearchMatches_emptyQuery_returnsEmptyList() {
        val contact = ContactEntity(
            id = 1,
            fullName = "John Doe",
            nicknames = emptyList(),
            phoneNumbers = emptyList(),
            sanitizedPrimaryPhone = "",
            emails = emptyList(),
            socialProfiles = emptyList(),
            bankAccounts = emptyList()
        )

        val matches = SearchUtils.extractSearchMatches(contact, "   ")
        assertTrue(matches.isEmpty())
    }

    @Test
    fun extractSearchMatches_socialAndBankMatch() {
        val contact = ContactEntity(
            id = 2,
            fullName = "Jane Smith",
            nicknames = emptyList(),
            phoneNumbers = emptyList(),
            sanitizedPrimaryPhone = "",
            emails = emptyList(),
            socialProfiles = listOf(SocialProfile("Instagram", "@janesmith")),
            bankAccounts = listOf(BankAccount("Maybank", "Jane S", "1234567890")),
            companyName = "Acme Corp"
        )

        val socialMatches = SearchUtils.extractSearchMatches(contact, "janesmith")
        assertFalse(socialMatches.isEmpty())

        val bankMatches = SearchUtils.extractSearchMatches(contact, "Maybank")
        assertFalse(bankMatches.isEmpty())
        assertEquals("BANK INFO", bankMatches.first().fieldName)
    }
}
