package com.mail2dev.upperdot.utils

import android.content.Context
import android.net.Uri
import com.mail2dev.upperdot.util.ImageCompressor
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

    suspend fun saveUriWithOptionalCompression(
        context: Context,
        uri: Uri,
        shouldCompress: Boolean,
        folderName: String = "attachments"
    ): String? {
        return if (shouldCompress && context.contentResolver.getType(uri)?.startsWith("image") == true) {
            try {
                val compressedFile = ImageCompressor.compressImage(context, uri)
                if (compressedFile != null) {
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val folder = File(context.filesDir, folderName)
                    if (!folder.exists()) folder.mkdirs()
                    val destinationFile = File(folder, fileName)
                    
                    compressedFile.inputStream().use { input ->
                        FileOutputStream(destinationFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    compressedFile.delete()
                    destinationFile.absolutePath
                } else {
                    copyUriToInternalStorage(context, uri, folderName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                copyUriToInternalStorage(context, uri, folderName)
            }
        } else {
            copyUriToInternalStorage(context, uri, folderName)
        }
    }

    fun getSafeAbsolutePath(context: Context, pathString: String?): String? {
        if (pathString.isNullOrEmpty()) return null
        val file = File(pathString)
        val folder = File(context.filesDir, "attachments")
        if (!folder.exists()) folder.mkdirs()
        return File(folder, file.name).absolutePath
    }
}
