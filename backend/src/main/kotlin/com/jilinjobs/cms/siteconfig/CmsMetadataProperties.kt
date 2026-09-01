package com.jilinjobs.cms.siteconfig

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "cms.metadata")
class CmsMetadataProperties {
    var sitePropertyGroups: LinkedHashMap<String, SitePropertyGroupMetadata> = linkedMapOf()

    fun sitePropertyGroupDefinitions(): List<SitePropertyGroupDefinition> = sitePropertyGroups.entries
        .map { (code, metadata) -> SitePropertyGroupDefinition(code.uppercase(), metadata.name, metadata.order) }
        .sortedWith(compareBy<SitePropertyGroupDefinition> { it.order }.thenBy { it.code })
}

data class SitePropertyGroupMetadata(
    var name: String = "",
    var order: Int = 0,
)

data class SitePropertyGroupDefinition(
    val code: String,
    val name: String,
    val order: Int,
)
