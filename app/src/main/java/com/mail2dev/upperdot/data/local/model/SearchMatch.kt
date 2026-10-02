package com.mail2dev.upperdot.data.local.model

import com.mail2dev.upperdot.data.local.entity.ContactEntity

data class SearchMatch(
    val fieldName: String,
    val snippet: String,
    val matchRanges: List<IntRange>
)

data class ContactSearchResult(
    val contact: ContactEntity,
    val matches: List<SearchMatch>
)
