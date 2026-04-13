package com.dripin.app.feature.capture

import com.dripin.app.core.model.ContentType

data class IncomingSharePayload(
    val contentType: ContentType,
    val sharedText: String? = null,
    val sharedUrl: String? = null,
    val sharedImageUri: String? = null,
    val sourceAppPackage: String? = null,
    val sourceAppLabel: String? = null,
)
