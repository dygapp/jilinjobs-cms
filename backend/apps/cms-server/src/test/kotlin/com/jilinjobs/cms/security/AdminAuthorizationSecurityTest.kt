package com.jilinjobs.cms.security

import com.jilinjobs.cms.advertisement.AdvertisementService
import com.jilinjobs.cms.advertisement.PublicAdvertisementQueryService
import com.jilinjobs.cms.column.ColumnService
import com.jilinjobs.cms.content.AdminArticleQueryService
import com.jilinjobs.cms.content.ArticleService
import com.jilinjobs.cms.content.PublicArticleSummaryQueryService
import com.jilinjobs.cms.identity.CmsPrincipal
import com.jilinjobs.cms.identity.CmsRole
import com.jilinjobs.cms.listing.CmsListService
import com.jilinjobs.cms.listing.PublicCmsListQueryService
import com.jilinjobs.cms.navigation.NavigationLocationService
import com.jilinjobs.cms.navigation.NavigationService
import com.jilinjobs.cms.page.PageService
import com.jilinjobs.cms.resource.ResourceService
import com.jilinjobs.cms.siteconfig.SiteConfigService
import com.jilinjobs.cms.staticresource.StaticResourceService
import java.nio.file.Files
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@WebMvcTest
@Import(CmsSecurityConfiguration::class)
class AdminAuthorizationSecurityTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var handlerMapping: RequestMappingHandlerMapping
    @Autowired(required = false) var userDetailsService: UserDetailsService? = null

    @MockitoBean lateinit var advertisementService: AdvertisementService
    @MockitoBean lateinit var publicAdvertisementQueryService: PublicAdvertisementQueryService
    @MockitoBean lateinit var columnService: ColumnService
    @MockitoBean lateinit var articleService: ArticleService
    @MockitoBean lateinit var adminArticleQueryService: AdminArticleQueryService
    @MockitoBean lateinit var publicArticleSummaryQueryService: PublicArticleSummaryQueryService
    @MockitoBean lateinit var cmsListService: CmsListService
    @MockitoBean lateinit var publicCmsListQueryService: PublicCmsListQueryService
    @MockitoBean lateinit var navigationService: NavigationService
    @MockitoBean lateinit var navigationLocationService: NavigationLocationService
    @MockitoBean lateinit var pageService: PageService
    @MockitoBean lateinit var resourceService: ResourceService
    @MockitoBean lateinit var siteConfigService: SiteConfigService
    @MockitoBean lateinit var staticResourceService: StaticResourceService

    @Test
    fun `anonymous admin request is 401 with diagnostic json`() {
        mockMvc.perform(get("/api/admin/columns"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.message").isString)
    }

    @Test
    fun `anonymous admin write is 401 before csrf can mask authentication failure`() {
        mockMvc.perform(post("/api/admin/columns"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").isString)
    }

    @Test
    fun `authenticated admin write still requires csrf unless its real provider opts into a stateless api policy`() {
        mockMvc.perform(
            post("/api/admin/columns")
                .with(authentication(principal(CmsRole.ADMIN).toSpringAuthentication())),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `authenticated identity without cms authority is rejected by method authorization`() {
        val verifiedButUnauthorized = UsernamePasswordAuthenticationToken.authenticated("verified-user", null, emptyList())
        mockMvc.perform(get("/api/admin/columns").with(authentication(verifiedButUnauthorized)))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").isString)
    }

    @Test
    fun `admin and super authorities can enter existing admin operation`() {
        mockMvc.perform(get("/api/admin/columns").with(authentication(principal(CmsRole.ADMIN).toSpringAuthentication())))
            .andExpect(status().isOk)
        mockMvc.perform(get("/api/admin/columns").with(authentication(principal(CmsRole.SUPER).toSpringAuthentication())))
            .andExpect(status().isOk)
    }

    @Test
    fun `public get and static resource remain anonymous`() {
        mockMvc.perform(get("/api/public/navigations"))
            .andExpect(status().isOk)

        val file = Files.createTempFile("cms-security-public-", ".txt")
        try {
            Files.writeString(file, "public")
            Mockito.`when`(staticResourceService.resolvePublic("security-test.txt")).thenReturn(file)
            mockMvc.perform(get("/static/security-test.txt"))
                .andExpect(status().isOk)
        } finally {
            Files.deleteIfExists(file)
        }
    }

    @Test
    fun `formal mvc slice has no default user details service`() {
        assertEquals(null, userDetailsService)
    }

    @Test
    fun `registered admin handlers exactly match contract and all carry method authorization`() {
        val adminHandlers = handlerMapping.handlerMethods.filterKeys { info ->
            info.patternValues.any { pattern -> pattern.startsWith("/api/admin/") }
        }
        assertTrue(adminHandlers.isNotEmpty())

        adminHandlers.values.forEach { handler ->
            assertTrue(
                AnnotatedElementUtils.hasAnnotation(handler.beanType, CmsAdminAccess::class.java),
                "Admin handler is missing @CmsAdminAccess: ${handler.beanType.name}#${handler.method.name}",
            )
        }

        val actual = adminHandlers.flatMap { (info, _) ->
            info.patternValues
                .filter { it.startsWith("/api/admin/") }
                .flatMap { pattern -> info.methodsCondition.methods.map { method -> "${method.name} $pattern" } }
        }.toSet()

        assertEquals(expectedAdminEndpoints, actual)
    }

    @Test
    fun `cms principal maps only trusted cms roles to spring authorities`() {
        val admin = principal(CmsRole.ADMIN).toSpringAuthentication()
        assertEquals(setOf(CmsAuthorities.ADMIN), admin.authorities.map { it.authority }.toSet())
        assertTrue(admin.isAuthenticated)
        assertTrue(admin.principal is CmsPrincipal)
        assertFalse(admin.authorities.any { it.authority?.startsWith("ROLE_") == true })
    }

    private fun principal(role: CmsRole): CmsPrincipal =
        CmsPrincipal.fromVerified("security-test", "${role.code}-user", setOf(role))

    private companion object {
        val expectedAdminEndpoints = setOf(
            "GET /api/admin/columns",
            "POST /api/admin/columns",
            "PUT /api/admin/columns/{id}",
            "DELETE /api/admin/columns/{id}",
            "GET /api/admin/articles",
            "POST /api/admin/articles",
            "GET /api/admin/articles/{id}",
            "PUT /api/admin/articles/{id}",
            "POST /api/admin/articles/{id}/publish",
            "POST /api/admin/articles/{id}/withdraw",
            "GET /api/admin/navigation-locations",
            "POST /api/admin/navigation-locations",
            "PUT /api/admin/navigation-locations/{code}",
            "DELETE /api/admin/navigation-locations/{code}",
            "GET /api/admin/navigations",
            "POST /api/admin/navigations",
            "PUT /api/admin/navigations/{id}",
            "DELETE /api/admin/navigations/{id}",
            "GET /api/admin/page-groups",
            "POST /api/admin/page-groups",
            "PUT /api/admin/page-groups/{id}",
            "GET /api/admin/pages",
            "POST /api/admin/pages",
            "PUT /api/admin/pages/{id}",
            "DELETE /api/admin/pages/{id}",
            "GET /api/admin/lists",
            "POST /api/admin/lists",
            "PUT /api/admin/lists/{id}",
            "DELETE /api/admin/lists/{id}",
            "GET /api/admin/lists/{id}/items",
            "POST /api/admin/lists/{id}/items",
            "PUT /api/admin/lists/{listId}/items/{itemId}",
            "DELETE /api/admin/lists/{listId}/items/{itemId}",
            "GET /api/admin/advertisements/slots",
            "POST /api/admin/advertisements/slots",
            "PUT /api/admin/advertisements/slots/{id}",
            "DELETE /api/admin/advertisements/slots/{id}",
            "GET /api/admin/advertisements/slots/{id}/items",
            "POST /api/admin/advertisements/slots/{id}/items",
            "PUT /api/admin/advertisements/slots/{slotId}/items/{adId}",
            "DELETE /api/admin/advertisements/slots/{slotId}/items/{adId}",
            "GET /api/admin/site-config",
            "POST /api/admin/site-config",
            "GET /api/admin/site-config/groups",
            "PUT /api/admin/site-config/{key}",
            "DELETE /api/admin/site-config/{key}",
            "PUT /api/admin/site-config/{key}/definition",
            "POST /api/admin/resources",
            "GET /api/admin/resources/{id}",
            "GET /api/admin/resources/{id}/content",
            "GET /api/admin/static-resources",
            "POST /api/admin/static-resources",
            "DELETE /api/admin/static-resources",
            "GET /api/admin/static-resources/trash",
            "POST /api/admin/static-resources/restore/{id}",
        )
    }
}
