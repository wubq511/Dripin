package com.dripin.app.feature.capture

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.dripin.app.core.model.ContentType

object ShareIntentParser {
    private val urlRegex = Regex("""https?://[^\s]+""", RegexOption.IGNORE_CASE)

    fun parse(
        intent: Intent,
        sourcePackage: String?,
        sourceLabel: String?,
    ): IncomingSharePayload {
        val type = intent.type.orEmpty()
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)?.trim()
        val chooserTitle = intent.getStringExtra(Intent.EXTRA_TITLE)?.trim()
        val imageUris = intent.readSharedImageUris()
        val extractedUrl = text?.extractFirstUrl()
        val cleanedText = text?.removeUrls()?.normalizeWhitespace()
        val initialTitle = listOf(subject, chooserTitle, cleanedText)
            .firstOrNull { !it.isNullOrBlank() }

        return when {
            type.startsWith("image/") && imageUris.isNotEmpty() -> {
                IncomingSharePayload(
                    contentType = ContentType.IMAGE,
                    initialTitle = initialTitle,
                    sharedImageUris = imageUris,
                    sourceAppPackage = sourcePackage,
                    sourceAppLabel = sourceLabel,
                )
            }

            extractedUrl != null -> {
                IncomingSharePayload(
                    contentType = ContentType.LINK,
                    initialTitle = initialTitle?.takeUnless { it == extractedUrl },
                    sharedText = cleanedText?.takeUnless { it == initialTitle },
                    sharedUrl = extractedUrl,
                    sourceAppPackage = sourcePackage,
                    sourceAppLabel = sourceLabel,
                )
            }

            else -> {
                IncomingSharePayload(
                    contentType = ContentType.TEXT,
                    sharedText = text,
                    initialTitle = listOf(subject, chooserTitle).firstOrNull { !it.isNullOrBlank() },
                    sourceAppPackage = sourcePackage,
                    sourceAppLabel = sourceLabel,
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.readSharedImageUris(): List<String> {
        val uris = buildList {
            val singleUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            }
            singleUri?.let(::add)

            val multipleUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            }
            multipleUris?.let(::addAll)

            val clipData = clipData
            if (clipData != null) {
                repeat(clipData.itemCount) { index ->
                    clipData.getItemAt(index).uri?.let(::add)
                }
            }
        }

        return uris.map(Uri::toString).distinct()
    }

    private fun String.extractFirstUrl(): String? {
        return urlRegex.find(this)
            ?.value
            ?.trimEnd('.', ',', '，', '。', '）', ')')
    }

    private fun String.removeUrls(): String {
        return replace(urlRegex, " ")
    }

    private fun String.normalizeWhitespace(): String? {
        return lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .joinToString(separator = "\n")
            .ifBlank { null }
    }
}
