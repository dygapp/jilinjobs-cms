package com.jilinjobs.cms.identity

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val now: Instant = Instant.parse("2026-09-29T00:00:00Z")
private val fixedClock: Clock = Clock.fixed(now, ZoneOffset.UTC)

class CmsIdentityConverterTest {
    @Test
    fun `same user ID from two verified sources remains distinct and mapped roles are isolated`() {
        AnnotationConfigApplicationContext(TestIdentityConfiguration::class.java).use { context ->
            val platform = context.getBean("platformVerifier", TestCredentialVerifier::class.java)
            val standalone = context.getBean("standaloneVerifier", TestCredentialVerifier::class.java)
            val converter = context.getBean(CmsIdentityConverter::class.java)
            val platformToken = platform.issue("same-user", setOf("editor"), now.plusSeconds(60))
            val standaloneToken = standalone.issue("same-user", setOf("owner"), now.plusSeconds(60))

            val first = converter.authenticate("platform", platformToken)
            val second = converter.authenticate("standalone", standaloneToken)
            assertNotEquals(first.operatorId, second.operatorId)
            assertEquals(CmsOperatorId("platform", "same-user"), first.operatorId)
            assertEquals(CmsOperatorId("standalone", "same-user"), second.operatorId)
            assertEquals(setOf(CmsRole.ADMIN), first.roles)
            assertEquals(setOf(CmsRole.SUPER), second.roles)
            assertThrows(UnsupportedOperationException::class.java) {
                (first.roles as MutableSet<CmsRole>).add(CmsRole.SUPER)
            }
            assertFalse(first.roles.contains(CmsRole.SUPER))
        }
    }

    @Test
    fun `unknown source, tampered, expired and cross-source credentials all fail closed`() {
        val platform = TestCredentialVerifier("platform", fixedClock)
        val standalone = TestCredentialVerifier("standalone", fixedClock)
        val converter = converter(platform, standalone)
        val valid = platform.issue("user-1", setOf("editor"), now.plusSeconds(1))
        val expired = platform.issue("user-1", setOf("editor"), now)
        val tampered = valid.dropLast(1) + if (valid.last() == 'x') "y" else "x"

        assertDenied { converter.authenticate("unknown", valid) }
        assertDenied { converter.authenticate("platform", "forged") }
        assertDenied { converter.authenticate("platform", tampered) }
        assertDenied { converter.authenticate("platform", expired) }
        assertDenied { converter.authenticate("standalone", valid) }
        assertDenied { converter.authenticate("platform", "") }
    }

    @Test
    fun `unmapped and empty provider roles cannot become CMS roles`() {
        val platform = TestCredentialVerifier("platform", fixedClock)
        val converter = converter(platform)
        assertDenied { converter.authenticate("platform", platform.issue("user-1", emptySet(), now.plusSeconds(60))) }
        assertDenied { converter.authenticate("platform", platform.issue("user-1", setOf("super"), now.plusSeconds(60))) }
        assertDenied { converter.authenticate("platform", platform.issue("user-1", setOf("editor", "unknown"), now.plusSeconds(60))) }
        assertDenied { converter.authenticate("platform", platform.issue(" ", setOf("editor"), now.plusSeconds(60))) }
        val noMapping = CmsIdentityConverter(listOf(platform), emptyMap())
        assertDenied { noMapping.authenticate("platform", platform.issue("user-1", setOf("editor"), now.plusSeconds(60))) }
    }

    @Test
    fun `duplicate provider IDs are rejected before any credential is accepted`() {
        assertThrows(IllegalArgumentException::class.java) {
            converter(TestCredentialVerifier("platform", fixedClock), TestCredentialVerifier("platform", fixedClock))
        }
    }

    @Test
    fun `provider failure never creates a principal`() {
        val broken = object : CredentialVerifier {
            override val identitySource = "platform"
            override fun verify(credential: String): VerifiedIdentity = error("provider unavailable")
        }
        assertDenied { converter(broken).authenticate("platform", "opaque-credential") }
    }

    private fun converter(vararg verifiers: CredentialVerifier): CmsIdentityConverter = CmsIdentityConverter(
        verifiers.toList(),
        mapOf("platform" to mapOf("editor" to CmsRole.ADMIN), "standalone" to mapOf("owner" to CmsRole.SUPER)),
    )

    private fun assertDenied(action: () -> Unit) {
        assertThrows(CmsAuthenticationException::class.java, action)
    }
}

@Configuration(proxyBeanMethods = false)
class TestIdentityConfiguration {
    @Bean("platformVerifier")
    fun platformVerifier(): TestCredentialVerifier = TestCredentialVerifier("platform", fixedClock)

    @Bean("standaloneVerifier")
    fun standaloneVerifier(): TestCredentialVerifier = TestCredentialVerifier("standalone", fixedClock)

    @Bean
    fun identityConverter(
        @Qualifier("platformVerifier") platform: TestCredentialVerifier,
        @Qualifier("standaloneVerifier") standalone: TestCredentialVerifier,
    ): CmsIdentityConverter = CmsIdentityConverter(
        listOf(platform, standalone),
        mapOf("platform" to mapOf("editor" to CmsRole.ADMIN), "standalone" to mapOf("owner" to CmsRole.SUPER)),
    )
}
