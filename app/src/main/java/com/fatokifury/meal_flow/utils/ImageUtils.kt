// Create a new file, e.g., utils/ImageUtils.kt
package com.fatokifury.meal_flow.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ImageUtils {

    /**
     * Copies an image from a given URI to the app's internal storage.
     * @param context The application context.
     * @param uri The temporary content URI of the image to copy.
     * @return The absolute path to the newly created permanent file, or null on failure.
     */
    fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            // Create a destination file in the app's private "images" directory
            val destinationDir = File(context.filesDir, "images")
            if (!destinationDir.exists()) {
                destinationDir.mkdirs()
            }
            val fileName = "recipe_${UUID.randomUUID()}.jpg"
            val destinationFile = File(destinationDir, fileName)

            // Use the content resolver to open an input stream and copy the file
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            // Return the permanent, accessible path to the new file
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if anything goes wrong
        }
    }
}
