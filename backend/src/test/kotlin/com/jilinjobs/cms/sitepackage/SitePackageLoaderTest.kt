package com.jilinjobs.cms.sitepackage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class SitePackageLoaderTest {
    @TempDir
    lateinit var tempDir: Path

    private val loader = SitePackageLoader(jacksonObjectMapper())

    @Test
    fun `loads a valid package with verified digest`() {
        val root = packageRoot()
        writeColumns(root, validColumns())
        writeManifest(root, "structure/columns.json", sha256(root.resolve("structure/columns.json")))

        val loaded = loader.load(root)

        assertEquals("loader-fixture", loaded.manifest.packageId)
        assertEquals(listOf("root", "child"), loaded.columns.map { it.alias })
    }

    @Test
    fun `rejects path traversal even when the external file digest is valid`() {
        val root = packageRoot()
        val outside = tempDir.resolve("outside.json")
        Files.writeString(outside, validColumns())
        writeManifest(root, "../outside.json", sha256(outside))

        assertThrows(IllegalArgumentException::class.java) { loader.load(root) }
    }

    @Test
    fun `rejects digest mismatch`() {
        val root = packageRoot()
        writeColumns(root, validColumns())
        writeManifest(root, "structure/columns.json", "0".repeat(64))

        assertThrows(IllegalArgumentException::class.java) { loader.load(root) }
    }

    @Test
    fun `rejects duplicate stable aliases`() {
        val root = packageRoot()
        writeColumns(
            root,
            """
            {
              "columns": [
                {"alias":"same","name":"One"},
                {"alias":"same","name":"Two"}
              ]
            }
            """.trimIndent() + "\n",
        )
        writeManifest(root, "structure/columns.json", sha256(root.resolve("structure/columns.json")))

        assertThrows(IllegalArgumentException::class.java) { loader.load(root) }
    }

    @Test
    fun `rejects cyclic parent relationships`() {
        val root = packageRoot()
        writeColumns(
            root,
            """
            {
              "columns": [
                {"alias":"one","name":"One","parentAlias":"two"},
                {"alias":"two","name":"Two","parentAlias":"one"}
              ]
            }
            """.trimIndent() + "\n",
        )
        writeManifest(root, "structure/columns.json", sha256(root.resolve("structure/columns.json")))

        assertThrows(IllegalArgumentException::class.java) { loader.load(root) }
    }

    private fun packageRoot(): Path = tempDir.resolve("package").also {
        Files.createDirectories(it.resolve("structure"))
    }

    private fun writeColumns(root: Path, content: String) {
        Files.writeString(root.resolve("structure/columns.json"), content)
    }

    private fun writeManifest(root: Path, structurePath: String, digest: String) {
        Files.writeString(
            root.resolve("manifest.json"),
            """
            {
              "packageId": "loader-fixture",
              "schemaVersion": 1,
              "version": "1",
              "structure": [
                {
                  "kind": "columns",
                  "path": "$structurePath",
                  "sha256": "$digest"
                }
              ]
            }
            """.trimIndent() + "\n",
        )
    }

    private fun validColumns() =
        """
        {
          "columns": [
            {"alias":"root","name":"Root"},
            {"alias":"child","name":"Child","parentAlias":"root"}
          ]
        }
        """.trimIndent() + "\n"

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
