package com.mail2dev.upperdot.data.network

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class GoogleDriveService(context: Context) {

    private val credential = GoogleAccountCredential.usingOAuth2(
        context,
        listOf(DriveScopes.DRIVE_APPDATA),
    )

    private val driveService: Drive by lazy {
        Drive.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("UpperDot").build()
    }

    suspend fun getBackupFile(): File? = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(credential.context) ?: return@withContext null
        credential.selectedAccount = account.account

        val result = driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = 'upperdot_backup.zip'")
            .setFields("files(id, name, modifiedTime, size)")
            .execute()
        
        result.files.firstOrNull()
    }

    suspend fun downloadFile(fileId: String, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        driveService.files()[fileId].executeMediaAndDownloadTo(outputStream)
    }

    suspend fun uploadFile(localFile: java.io.File, existingFileId: String? = null): String = withContext(Dispatchers.IO) {
        val metadata = File().apply {
            name = "upperdot_backup.zip"
            if (existingFileId == null) {
                parents = listOf("appDataFolder")
            }
        }
        val content = FileContent("application/zip", localFile)

        if (existingFileId != null) {
            driveService.files().update(existingFileId, metadata, content).execute().id
        } else {
            driveService.files().create(metadata, content).execute().id
        }
    }
}
