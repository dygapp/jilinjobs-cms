package com.jilinjobs.cms.security

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletResponse

class ReviewIdentitySessionsTest {
    @Test
    fun `profiles issue distinct opaque credentials with server controlled identity`() {
        val sessions = sessions()
        val admin = sessions.create(ReviewIdentityProfile.ADMIN)
        val superSession = sessions.create(ReviewIdentityProfile.SUPER)
        assertTrue(admin.credential.matches(Regex("[A-Za-z0-9_-]{43}")))
        assertNotEquals(admin.credential, superSession.credential)
        assertEquals("local-review-admin", sessions.verify(admin.credential)?.userId)
        assertEquals(setOf("review-admin"), sessions.verify(admin.credential)?.externalRoles)
        assertEquals("local-review-super", sessions.verify(superSession.credential)?.userId)
        assertNull(sessions.verify(admin.credential + "tampered"))
        assertEquals("local-review-admin", sessions.verify("automation-admin")?.userId)
        assertEquals("local-review-super", sessions.verify("automation-super")?.userId)
    }

    @Test
    fun `expiry rejects credential at boundary and reclaims bounded capacity`() {
        val clock = MutableClock()
        val sessions = sessions(clock, maxSessions = 1)
        val old = sessions.create(ReviewIdentityProfile.ADMIN)
        assertThrows(ReviewIdentityCapacityException::class.java) { sessions.create(ReviewIdentityProfile.SUPER) }
        clock.now = old.expiresAt.minusNanos(1)
        assertEquals("local-review-admin", sessions.verify(old.credential)?.userId)
        clock.now = old.expiresAt
        assertNull(sessions.verify(old.credential))
        val replacement = sessions.create(ReviewIdentityProfile.SUPER)
        assertEquals("local-review-super", sessions.verify(replacement.credential)?.userId)
        clock.now = replacement.expiresAt
        sessions.create(ReviewIdentityProfile.ADMIN)
        assertNull(sessions.verify(replacement.credential))
    }

    @Test
    fun `logout and simulated expiry invalidate only current short session and clear preview cookie`() {
        val sessions = sessions()
        val controller = ReviewIdentitySessionController(sessions)
        val createResponse = MockHttpServletResponse()
        val admin = controller.create(CreateReviewIdentitySessionRequest("admin"), createResponse)
        val superSession = controller.create(CreateReviewIdentitySessionRequest("super"), MockHttpServletResponse())
        val cookie = createResponse.getHeader("Set-Cookie")!!
        assertTrue(cookie.contains("HttpOnly; SameSite=Strict"))
        assertTrue(cookie.contains("Path=/api/admin/resources/"))
        val logoutResponse = MockHttpServletResponse()
        controller.logout(admin.credential, logoutResponse)
        controller.logout(admin.credential, MockHttpServletResponse())
        assertNull(sessions.verify(admin.credential))
        assertEquals("local-review-super", sessions.verify(superSession.credential)?.userId)
        assertTrue(logoutResponse.getHeader("Set-Cookie")!!.contains("Max-Age=0"))
        controller.expire(superSession.credential, MockHttpServletResponse())
        assertNull(sessions.verify(superSession.credential))
        assertEquals("local-review-super", sessions.verify("automation-super")?.userId)
        assertThrows(InvalidReviewIdentityProfileException::class.java) {
            controller.create(CreateReviewIdentitySessionRequest("owner"), MockHttpServletResponse())
        }
    }

    @Test
    fun `unsafe runtime session configuration fails closed`() {
        assertThrows(IllegalArgumentException::class.java) { ReviewIdentitySessions("", "super", Duration.ofMinutes(30), 1) }
        assertThrows(IllegalArgumentException::class.java) { ReviewIdentitySessions("same", "same", Duration.ofMinutes(30), 1) }
        assertThrows(IllegalArgumentException::class.java) { ReviewIdentitySessions("admin", "super", Duration.ZERO, 1) }
        assertThrows(IllegalArgumentException::class.java) { ReviewIdentitySessions("admin", "super", Duration.ofHours(25), 1) }
        assertThrows(IllegalArgumentException::class.java) { ReviewIdentitySessions("admin", "super", Duration.ofMinutes(30), 0) }
    }

    private fun sessions(clock: Clock = Clock.systemUTC(), maxSessions: Int = 256) = ReviewIdentitySessions(
        "automation-admin", "automation-super", Duration.ofMinutes(30), maxSessions, clock,
    )

    private class MutableClock(var now: Instant = Instant.parse("2026-09-30T00:00:00Z")) : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
    }
}
