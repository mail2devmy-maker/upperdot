package com.mail2dev.upperdot.utils

import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDate(pattern: String = "MMM dd, yyyy • hh:mm a"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}
