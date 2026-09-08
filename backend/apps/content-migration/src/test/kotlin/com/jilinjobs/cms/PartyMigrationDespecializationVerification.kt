package com.jilinjobs.cms

import java.nio.file.Files
import java.nio.file.Path

private val quotedValue = Regex("\\\"([^\\\"]+)\\\"")
private val fingerprintValue = Regex("\\\"sourceFingerprint\\\"\\s*:\\s*\\\"([0-9a-f]{64})\\\"")
private val aliasValue = Regex("\\\"columnAlias\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
private val listCodeValue = Regex("\\\"listCode\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
private val legacyKeyValue = Regex("\\\"legacyKey\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
private val fromFingerprintValue = Regex("\\\"fromFingerprint\\\"\\s*:\\s*\\\"([0-9a-f]{64})\\\"")

fun main() {
    val backendRoot = Path.of(".").toAbsolutePath().normalize()
    val repositoryRoot = backendRoot.parent
    val migrationRoot = backendRoot.resolve("apps/content-migration/src/main/kotlin/com/jilinjobs/cms/migration")
    val genericRoot = migrationRoot.resolve("generic")
    val partyRoot = migrationRoot.resolve("party")
    val dataRoot = repositoryRoot.resolve("data-migrations/party/v1")

    require(Files.isDirectory(genericRoot)) { "Generic migration source root missing" }
    require(Files.isDirectory(partyRoot)) { "Party adapter/compatibility source root missing" }
    require(Files.isDirectory(dataRoot)) { "Party canonical dataset root missing" }

    val genericSource = kotlinSource(genericRoot)
    val partySource = kotlinSource(partyRoot)
    val compatibilityFile = dataRoot.resolve("compatibility.json")
    require(Files.isRegularFile(compatibilityFile)) { "Party compatibility authority missing" }
    val compatibility = Files.readString(compatibilityFile)

    require("importPreparedDataset" in genericSource) { "Generic prepared-dataset entry missing" }
    require("sourceProvenanceUrl" in genericSource) { "Generic prepared provenance field missing" }
    require("staticTarget" in genericSource) { "Generic prepared static target field missing" }
    require("CanonicalFileVerifier" in genericSource) { "Generic shared file verifier missing" }

    val genericForbidden = listOf(
        "party-",
        "PARTY_CAROUSEL",
        "party-carousel:position:",
        "EU29",
        "EU30",
        "migrated/party/",
    )
    genericForbidden.forEach { token ->
        require(token !in genericSource) { "Generic migration source contains Party compatibility identity: $token" }
    }

    val manifest = Files.readString(dataRoot.resolve("manifest.json"))
    val aliases = aliasValue.findAll(manifest).map { it.groupValues[1] }.toSet()
    require(aliases.isNotEmpty()) { "Party manifest exposes no columnAlias authority" }
    aliases.forEach { alias ->
        require("\"$alias\"" !in partySource) { "Party Kotlin contains canonical current column alias: $alias" }
    }

    val listIndexes = Files.walk(dataRoot.resolve("lists")).use { stream ->
        stream.filter { it.fileName.toString() == "index.json" }.toList()
    }
    require(listIndexes.size == 1) { "Phase 2C focused verifier expects the current bounded Party dataset to expose one list" }
    val listIndex = Files.readString(listIndexes.single())
    val listCode = requireNotNull(listCodeValue.find(listIndex)?.groupValues?.get(1)) { "Current Party listCode missing" }
    require("\"$listCode\"" !in partySource) { "Party Kotlin contains current list identity: $listCode" }

    val itemFiles = Files.walk(listIndexes.single().parent).use { stream ->
        stream.filter { it.fileName.toString() == "item.json" }.toList()
    }
    require(itemFiles.isNotEmpty()) { "Current Party list has no canonical items" }
    val currentFingerprints = mutableSetOf<String>()
    val currentLegacyKeys = mutableSetOf<String>()
    itemFiles.forEach { itemFile ->
        val text = Files.readString(itemFile)
        fingerprintValue.find(text)?.groupValues?.get(1)?.let(currentFingerprints::add)
        legacyKeyValue.find(text)?.groupValues?.get(1)?.let(currentLegacyKeys::add)
    }
    currentFingerprints.forEach { fingerprint ->
        require(fingerprint !in partySource) { "Party Kotlin contains current canonical fingerprint: $fingerprint" }
        require(fingerprint !in compatibility) { "Compatibility authority duplicates current toFingerprint: $fingerprint" }
    }
    currentLegacyKeys.forEach { legacyKey ->
        require("\"$legacyKey\"" !in partySource) { "Party Kotlin contains current canonical legacy identity: $legacyKey" }
    }

    require(Regex("\\\"compatibilityVersion\\\"\\s*:\\s*1").containsMatchIn(compatibility)) {
        "Party compatibilityVersion must be 1"
    }
    val fromFingerprints = fromFingerprintValue.findAll(compatibility).map { it.groupValues[1] }.toList()
    require(fromFingerprints.isNotEmpty()) { "Party compatibility authority contains no old-state fingerprint" }
    fromFingerprints.forEach { fingerprint ->
        require(fingerprint !in partySource) { "Party Kotlin contains compatibility-only old fingerprint: $fingerprint" }
        require(fingerprint !in genericSource) { "Generic Kotlin contains compatibility-only old fingerprint: $fingerprint" }
    }
    listOf(
        "\"toFingerprint\"",
        "\"toSourceType\"",
        "\"articleRef\"",
        "\"imageSha256\"",
        "\"sourceOrder\"",
        "\"title\"",
        "\"url\"",
    ).forEach { forbiddenKey ->
        require(forbiddenKey !in compatibility) { "Compatibility authority duplicates current canonical state: $forbiddenKey" }
    }

    val legacyEntrypoints = listOf(
        migrationRoot.resolve("PartyHistoricalContentMigration.kt"),
        migrationRoot.resolve("PartyHistoricalContentMigrationV2.kt"),
        migrationRoot.resolve("PartyCarouselMigration.kt"),
        migrationRoot.resolve("PartyCarouselMigrationV2.kt"),
    )
    val directRuntimeTokens = listOf(
        "ArticleService",
        "ResourceService",
        "CmsListService",
        "StaticResourceService",
        "CmsListMapper",
        "@Transactional",
    )
    legacyEntrypoints.forEach { file ->
        require(Files.isRegularFile(file)) { "Legacy Party entrypoint missing: ${file.fileName}" }
        val text = Files.readString(file)
        directRuntimeTokens.forEach { token ->
            require(token !in text) { "Legacy Party entrypoint still owns direct Runtime mutation: ${file.fileName} / $token" }
        }
    }

    require("PartyCanonicalAdapter" in partySource) { "Bounded Party canonical adapter missing" }
    require("PartyCompatibilityService" in partySource) { "Bounded Party compatibility service missing" }
    require("GenericContentMigrationService" in partySource) { "Party steady-state path is not delegated to Generic engine" }
    require("migrated/party/carousel" in partySource) { "Accepted Party static projection continuity is missing" }

    println("PARTY_MIGRATION_DESPECIALIZATION_VERIFY PASS")
}

private fun kotlinSource(root: Path): String = Files.walk(root).use { stream ->
    stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
        .sorted()
        .map(Files::readString)
        .toList()
        .joinToString("\n")
}
