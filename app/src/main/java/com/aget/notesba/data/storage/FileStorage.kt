package com.aget.notesba.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileStorage(
    private val context: Context
) {

    private val attachmentsDirectory: File
        get() {
            val directory =
                File(
                    context.filesDir,
                    "attachments"
                )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            return directory
        }

    fun saveDrawing(
        bitmap: Bitmap
    ): String {

        val file = File(
            attachmentsDirectory,
            "${UUID.randomUUID()}.png"
        )

        FileOutputStream(file).use { output ->

            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                output
            )
        }

        bitmap.recycle()

        return file.absolutePath
    }

    fun copyFromUri(
        uri: Uri,
        extension: String,

        ): String {

        val extension =
            getExtension(uri)

        val file = File(
            attachmentsDirectory,
            "${UUID.randomUUID()}.$extension"
        )

        context.contentResolver
            .openInputStream(uri)
            ?.use { input ->

                file.outputStream().use { output ->
                    input.copyTo(output)
                }

            } ?: throw IllegalStateException(
            "No se pudo abrir el archivo"
        )

        return file.absolutePath
    }

    private fun getExtension(
        uri: Uri
    ): String {

        val mimeType =
            context.contentResolver
                .getType(uri)

        return MimeTypeMap
            .getSingleton()
            .getExtensionFromMimeType(
                mimeType
            )
            ?: "bin"
    }

    fun delete(
        path: String?
    ) {

        if (path == null) {
            return
        }

        runCatching {
            File(path).delete()
        }
    }
}
