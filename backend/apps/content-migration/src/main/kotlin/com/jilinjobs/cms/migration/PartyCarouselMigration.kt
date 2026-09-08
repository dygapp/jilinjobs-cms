package com.jilinjobs.cms.migration

data class PartyCarouselSnapshotImage(
    val sourceUrl: String,
    val originalReference: String,
    val snapshotPath: String,
    val sha256: String,
    val contentType: String?,
    val sizeBytes: Long,
)

data class PartyCarouselSnapshotEvidence(
    val sourceHref: String,
    val sourceImageReference: String,
    val sourceTarget: String,
)

data class PartyCarouselCanonicalIndexItem(
    val legacyKey: String,
    val path: String,
    val sourceOrder: Int,
    val sourceFingerprint: String,
)

data class PartyCarouselCanonicalIndex(
    val listCode: String,
    val sourceSystem: String,
    val sourcePage: String,
    val items: List<PartyCarouselCanonicalIndexItem>,
)
