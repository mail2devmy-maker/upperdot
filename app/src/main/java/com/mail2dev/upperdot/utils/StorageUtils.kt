package com.mail2dev.upperdot.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object StorageUtils {
    fun copyUriToInternalStorage(context: Context, uri: Uri, folderName: String = "attachments"): String? {
        return try {
            val contentResolver = context.contentResolver
            val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
            val fileName = "${UUID.randomUUID()}.$extension"
            
            val folder = File(context.filesDir, folderName)
            if (!folder.exists()) folder.mkdirs()
            
            val destinationFile = File(folder, fileName)
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
