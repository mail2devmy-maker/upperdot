package com.mail2dev.upperdot.data.repository

import com.mail2dev.upperdot.data.local.dao.PreferenceDao
import com.mail2dev.upperdot.data.local.entity.PreferenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceRepository(private val preferenceDao: PreferenceDao) {

    val preferences: Flow<PreferenceEntity> = preferenceDao.getPreferences().map { 
        it ?: PreferenceEntity() 
    }

    suspend fun savePreferences(preferences: PreferenceEntity) {
        preferenceDao.savePreferences(preferences)
    }
}
