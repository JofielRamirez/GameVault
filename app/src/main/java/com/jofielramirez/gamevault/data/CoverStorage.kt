package com.jofielramirez.gamevault.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Saves covers picked from the photo gallery inside the app's private storage.
 *
 * The photo picker only grants temporary access to the picked image, so the app keeps its own
 * copy, reduced to at most [MAX_SIDE] pixels so a 12 MP photo does not take several megabytes.
 */
class CoverStorage(context: Context) {

    private val directory = File(context.applicationContext.filesDir, "covers")
    private val resolver = context.applicationContext.contentResolver

    /** Returns the file:// URI of the saved copy, or null if the image could not be read. */
    suspend fun save(source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
            val bitmap = resolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching null

            directory.mkdirs()
            val file = File(directory, "${UUID.randomUUID()}.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            bitmap.recycle()
            Uri.fromFile(file).toString()
        }.getOrNull()
    }

    /** Removes every saved cover (used when the whole library is deleted). */
    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        directory.deleteRecursively()
    }

    private companion object {
        const val MAX_SIDE = 1200
    }
}
