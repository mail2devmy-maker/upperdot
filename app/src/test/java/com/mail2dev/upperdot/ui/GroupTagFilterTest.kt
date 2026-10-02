package com.mail2dev.upperdot.ui

import com.mail2dev.upperdot.data.local.entity.ContactEntity
import org.junit.Assert.*
import org.junit.Test

class GroupTagFilterTest {

    @Test
    fun testTwoTierFilterMatching() {
        val contact1 = ContactEntity(
            id = 1,
            fullName = "Alice Smith",
            nicknames = emptyList(),
            phoneNumbers = listOf("0123456789"),
            sanitizedPrimaryPhone = "123456789",
            emails = emptyList(),
            groupId = "wrk",
            tagId = "tag1",
            socialProfiles = emptyList(),
            bankAccounts = emptyList()
        )

        val contact2 = ContactEntity(
            id = 2,
            fullName = "Bob Jones",
            nicknames = emptyList(),
            phoneNumbers = listOf("0198765432"),
            sanitizedPrimaryPhone = "198765432",
            emails = emptyList(),
            groupId = "wrk",
            tagId = "tag2",
            socialProfiles = emptyList(),
            bankAccounts = emptyList()
        )

        val contact3 = ContactEntity(
            id = 3,
            fullName = "Charlie Brown",
            nicknames = emptyList(),
            phoneNumbers = listOf("0112233445"),
            sanitizedPrimaryPhone = "112233445",
            emails = emptyList(),
            groupId = "fav",
            tagId = null,
            socialProfiles = emptyList(),
            bankAccounts = emptyList()
        )

        val allContacts = listOf(contact1, contact2, contact3)

        // Filter: All Groups, All Tags
        val filter1 = allContacts.filter {
            val groupMatches = true
            val tagMatches = true
            groupMatches && tagMatches
        }
        assertEquals(3, filter1.size)

        // Filter: Group "wrk", All Tags
        val filter2 = allContacts.filter { contact ->
            val groupMatches = contact.groupId == "wrk"
            val tagMatches = true
            groupMatches && tagMatches
        }
        assertEquals(2, filter2.size)

        // Filter: Group "wrk", Tag "tag1"
        val filter3 = allContacts.filter { contact ->
            val groupMatches = contact.groupId == "wrk"
            val tagMatches = contact.tagId == "tag1"
            groupMatches && tagMatches
        }
        assertEquals(1, filter3.size)
        assertEquals("Alice Smith", filter3.first().fullName)
    }
}
