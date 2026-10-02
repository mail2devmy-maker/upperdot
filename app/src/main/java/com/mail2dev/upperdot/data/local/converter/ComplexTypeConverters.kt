package com.mail2dev.upperdot.data.local.converter

import androidx.room.TypeConverter
import com.mail2dev.upperdot.ui.add_contact.BankAccount
import com.mail2dev.upperdot.ui.add_contact.SocialProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ComplexTypeConverters {
    @TypeConverter
    fun fromSocialList(list: List<SocialProfile>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toSocialList(json: String): List<SocialProfile> {
        return try { Json.decodeFromString(json) } catch (e: Exception) { emptyList() }
    }

    @TypeConverter
    fun fromBankList(list: List<BankAccount>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toBankList(json: String): List<BankAccount> {
        return try { Json.decodeFromString(json) } catch (e: Exception) { emptyList() }
    }
}
