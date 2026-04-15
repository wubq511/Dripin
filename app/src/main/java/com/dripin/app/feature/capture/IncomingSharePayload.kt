package com.dripin.app.feature.capture

import com.dripin.app.core.model.ContentType

data class IncomingSharePayload(
    val contentType: ContentType,
    val initialTitle: String? = null,
    val sharedText: String? = null,
    val sharedUrl: String? = null,
    val sharedImageUris: List<String> = emptyList(),
    val sourceAppPackage: String? = null,
    val sourceAppLabel: String? = null,
    val isManualEntry: Boolean = false,
)
