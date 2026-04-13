package com.dripin.app.feature.capture

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.dripin.app.core.model.ContentType

object ShareIntentParser {
    fun parse(
        intent: Intent,
        sourcePackage: String?,
        sourceLabel: String?,
    ): IncomingSharePayload {
        val type = intent.type.orEmpty()
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        val imageUri = intent.readSharedImageUri()

        return when {
            type.startsWith("image/") && imageUri != null -> {
                IncomingSharePayload(
                    contentType = ContentType.IMAGE,
                    sharedImageUri = imageUri,
                    sourceAppPackage = sourcePackage,
                    sourceAppLabel = sourceLabel,
                )
            }

            text != null && text.startsWith("http", ignoreCase = true) -> {
                IncomingSharePayload(
                    contentType = ContentType.LINK,
                    sharedUrl = text,
                    sourceAppPackage = sourcePackage,
                    sourceAppLabel = sourceLabel,
                )
            }

            else -> {
                IncomingSharePayload(
                    contentType = ContentType.TEXT,
                    sharedText = text,
                    sourceAppPackage = sourcePackage,
                    sourceAppLabel = sourceLabel,
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.readSharedImageUri(): String? {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
        }
        return uri?.toString()
    }
}
