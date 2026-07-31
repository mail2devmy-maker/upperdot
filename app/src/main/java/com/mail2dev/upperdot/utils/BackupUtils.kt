package com.mail2dev.upperdot.utils

import android.content.Context
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupUtils {

    fun createZipBackup(
        filesDir: File,
        jsonContent: String,
        outputStream: OutputStream
    ) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
            // 1. Add database.json
            val dbEntry = ZipEntry("database.json")
            zos.putNextEntry(dbEntry)
            zos.write(jsonContent.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 2. Add attachments/ contents
            packageDirectory(filesDir, "attachments", zos)
            // 3. Add qrcodes/ contents
            packageDirectory(filesDir, "qrcodes", zos)
        }
    }

    private fun packageDirectory(baseDir: File, currentPath: String, zos: ZipOutputStream) {
        val dir = File(baseDir, currentPath)
        if (!dir.exists() || !dir.isDirectory) return

        dir.listFiles()?.forEach { file ->
            val zipPath = if (currentPath.isEmpty()) file.name else "$currentPath/${file.name}"
            if (file.isDirectory) {
                packageDirectory(baseDir, zipPath, zos)
            } else {
                val entry = ZipEntry(zipPath)
                zos.putNextEntry(entry)
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }

    fun restoreZipBackup(
        filesDir: File,
        inputStream: InputStream
    ): String? {
        var jsonContent: String? = null

        // Clear existing attachment folders for a clean restore
        File(filesDir, "attachments").deleteRecursively()
        File(filesDir, "qrcodes").deleteRecursively()

        ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val file = File(filesDir, entry.name)
                
                if (entry.name == "database.json") {
                    val baos = ByteArrayOutputStream()
                    zis.copyTo(baos)
                    jsonContent = baos.toString("UTF-8")
                } else {
                    // Restore files (attachments/, qrcodes/, etc)
                    file.parentFile?.let { if (!it.exists()) it.mkdirs() }
                    FileOutputStream(file).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return jsonContent
    }
}
