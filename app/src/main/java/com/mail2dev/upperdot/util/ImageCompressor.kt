package com.mail2dev.upperdot.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.min

object ImageCompressor {

    private const val MAX_DIMENSION = 1920
    private const val COMPRESSION_QUALITY = 80

    suspend fun compressImage(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            // First decode with inJustDecodeBounds=true to check dimensions
            context.contentResolver.openInputStream(uri)?.use { tempStream ->
                BitmapFactory.decodeStream(tempStream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)
            options.inJustDecodeBounds = false
            
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            if (bitmap == null) return@withContext null

            val scaledBitmap = scaleBitmap(bitmap)
            
            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(outputFile)
            
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()
            
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) {
            return bitmap
        }
        
        val ratio = min(MAX_DIMENSION.toFloat() / width, MAX_DIMENSION.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
