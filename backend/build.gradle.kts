import java.util.zip.ZipFile
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer

plugins {
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("jvm") version "2.3.20" apply false
    kotlin("plugin.spring") version "2.3.20" apply false
}

allprojects {
    group = "com.jilinjobs"
    version = "0.1.0-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

evaluationDependsOn(":modules:cms-core")
evaluationDependsOn(":apps:cms-server")
evaluationDependsOn(":apps:content-migration")

val serverProject = project(":apps:cms-server")
val migrationProject = project(":apps:content-migration")
val serverSourceSets = serverProject.extensions.getByType<SourceSetContainer>()
val migrationSourceSets = migrationProject.extensions.getByType<SourceSetContainer>()

tasks.register<Delete>("clean") {
    dependsOn(":modules:cms-core:clean", ":apps:cms-server:clean", ":apps:content-migration:clean")
    delete(layout.buildDirectory)
}

tasks.register("test") {
    group = "verification"
    description = "Run Core, Server and Content Migration unit tests"
    dependsOn(":modules:cms-core:test", ":apps:cms-server:test", ":apps:content-migration:test")
}

tasks.register("bootJar") {
    group = "build"
    description = "Build both executable applications while preserving the legacy CMS Server artifact path"
    dependsOn(":apps:cms-server:bootJar", ":apps:content-migration:bootJar")
    finalizedBy("verifyBackendApplicationBoundary")
}

tasks.register<JavaExec>("runContentMigration") {
    group = "migration"
    description = "Run the site-neutral canonical Content Migration application"
    dependsOn(":apps:content-migration:classes")
    classpath = migrationSourceSets.getByName("main").runtimeClasspath
    mainClass.set("com.jilinjobs.cms.ContentMigrationApplicationKt")
}

tasks.register<JavaExec>("provisionSitePackage") {
    group = "provisioning"
    description = "Reconcile versioned stable Site Package structure against an initialized CMS schema"
    dependsOn(":apps:cms-server:classes")
    classpath = serverSourceSets.getByName("main").runtimeClasspath
    mainClass.set("com.jilinjobs.cms.provisioning.SitePackageProvisioningCliKt")
}

tasks.register<JavaExec>("bootstrapSitePackage") {
    group = "provisioning"
    description = "Install stable Site Package structure and one-time current-schema operational defaults"
    dependsOn(":apps:cms-server:classes")
    classpath = serverSourceSets.getByName("main").runtimeClasspath
    mainClass.set("com.jilinjobs.cms.provisioning.SitePackageBootstrapCli")
}

fun JavaExec.configureServerVerification(mainClassName: String) {
    dependsOn(":apps:cms-server:testClasses")
    classpath = serverSourceSets.getByName("test").runtimeClasspath
    mainClass.set(mainClassName)
}

fun JavaExec.configureMigrationVerification(mainClassName: String) {
    dependsOn(":apps:content-migration:testClasses")
    classpath = migrationSourceSets.getByName("test").runtimeClasspath
    mainClass.set(mainClassName)
}

tasks.register<JavaExec>("verifySitePackageFoundation") {
    group = "verification"
    description = "Verify Site Package provisioning against a real MySQL V1 generic schema"
    configureServerVerification("com.jilinjobs.cms.provisioning.SitePackageFoundationVerificationKt")
}

tasks.register<JavaExec>("verifyStableSiteStructure") {
    group = "verification"
    description = "Verify stable Site Package structure against the current Generic CMS schema"
    configureServerVerification("com.jilinjobs.cms.provisioning.StableSiteStructureVerificationKt")
}

tasks.register<JavaExec>("verifyRuntimeSitePackageComposition") {
    group = "verification"
    description = "Verify ordinary Site Package runtime composition reconciles stable structure without operational bootstrap"
    configureServerVerification("com.jilinjobs.cms.provisioning.RuntimeSitePackageCompositionVerificationKt")
}

tasks.register<JavaExec>("verifySiteBootstrapBaselineSeparation") {
    group = "verification"
    description = "Verify EU-41 Generic Schema lineage and one-time JilinJobs operational bootstrap separation"
    configureServerVerification("com.jilinjobs.cms.provisioning.SiteBootstrapBaselineSeparationVerificationKt")
}

tasks.register<JavaExec>("verifySitePackageAssets") {
    group = "verification"
    description = "Verify EU-42 Site Package asset integrity, create-if-missing projection, protected paths and runtime preservation"
    configureServerVerification("com.jilinjobs.cms.provisioning.SitePackageAssetProjectionVerificationKt")
}

tasks.register<JavaExec>("verifyLinkOpenModeMigration") {
    group = "verification"
    description = "Verify EU-60 V4 to V5 link open-mode migration against real MySQL"
    configureServerVerification("com.jilinjobs.cms.migration.LinkOpenModeMigrationVerificationKt")
}

tasks.register<JavaExec>("verifyPageContentOwnership") {
    group = "verification"
    description = "Verify EU-49 Site Package Page structure does not reclaim operator-managed Page content"
    configureServerVerification("com.jilinjobs.cms.provisioning.PageContentOwnershipVerificationKt")
}

tasks.register<JavaExec>("verifyPageContentAdoption") {
    group = "verification"
    description = "Verify EU-52 exact-baseline Page content adoption and protected divergence reporting"
    configureServerVerification("com.jilinjobs.cms.provisioning.PageContentAdoptionVerificationKt")
}

tasks.register<JavaExec>("verifyContentMigrationBoundary") {
    group = "verification"
    description = "Verify Content Migration is non-web, excludes Server transport and composes only site-neutral migration capabilities"
    configureMigrationVerification("com.jilinjobs.cms.ContentMigrationBoundaryVerificationKt")
}

tasks.register<JavaExec>("verifyGenericContentMigration") {
    group = "verification"
    description = "Verify site-neutral canonical migration preflight, execution, idempotency and failure semantics"
    configureMigrationVerification("com.jilinjobs.cms.GenericContentMigrationVerificationKt")
}

tasks.register<JavaExec>("verifyGenericPageContentMigration") {
    group = "verification"
    description = "Verify EU-49 site-neutral canonical Page apply, idempotency, conflict and resource semantics"
    configureMigrationVerification("com.jilinjobs.cms.GenericPageContentMigrationVerificationKt")
}

tasks.register<JavaExec>("verifyGenericListItemCompatibility") {
    group = "verification"
    description = "Verify site-neutral ListItem compatibility guards, in-place transition and post-transition idempotency"
    configureMigrationVerification("com.jilinjobs.cms.GenericListItemCompatibilityVerificationKt")
}

tasks.register("verifyBackendApplicationBoundary") {
    group = "verification"
    description = "Inspect packaged Server and Migration BootJars for EU-46 application ownership boundaries"
    dependsOn(":apps:cms-server:bootJar", ":apps:content-migration:bootJar")
    doLast {
        val serverJar = layout.buildDirectory.file("libs/jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar").get().asFile
        val migrationJar = migrationProject.layout.buildDirectory.file("libs/jilinjobs-cms-content-migration-0.1.0-SNAPSHOT.jar").get().asFile
        require(serverJar.isFile) { "Server BootJar missing: $serverJar" }
        require(migrationJar.isFile) { "Migration BootJar missing: $migrationJar" }

        fun jarEntries(file: java.io.File): Set<String> = ZipFile(file).use { zip ->
            buildSet {
                val entries = zip.entries()
                while (entries.hasMoreElements()) add(entries.nextElement().name)
            }
        }

        val serverEntries = jarEntries(serverJar)
        require("BOOT-INF/classes/com/jilinjobs/cms/CmsApplication.class" in serverEntries) { "Server application class missing" }
        require(serverEntries.none { it.startsWith("BOOT-INF/classes/com/jilinjobs/cms/migration/") }) {
            "Server BootJar contains Content Migration classes"
        }

        val migrationEntries = jarEntries(migrationJar)
        require("BOOT-INF/classes/com/jilinjobs/cms/ContentMigrationApplication.class" in migrationEntries) { "Migration application class missing" }
        require(migrationEntries.any { it.startsWith("BOOT-INF/classes/com/jilinjobs/cms/migration/generic/GenericContentMigration") }) {
            "Migration BootJar is missing Generic content migration classes"
        }
        require(migrationEntries.any { it.startsWith("BOOT-INF/classes/com/jilinjobs/cms/migration/generic/GenericListItemCompatibility") }) {
            "Migration BootJar is missing Generic compatibility classes"
        }
        val forbiddenServerClasses = listOf(
            "BOOT-INF/classes/com/jilinjobs/cms/CmsApplication.class",
            "BOOT-INF/classes/com/jilinjobs/cms/common/ApiExceptionHandler.class",
            "BOOT-INF/classes/com/jilinjobs/cms/column/ColumnController.class",
            "BOOT-INF/classes/com/jilinjobs/cms/content/ArticleController.class",
            "BOOT-INF/classes/com/jilinjobs/cms/navigation/NavigationController.class",
            "BOOT-INF/classes/com/jilinjobs/cms/page/PageController.class",
            "BOOT-INF/classes/com/jilinjobs/cms/resource/ResourceController.class",
            "BOOT-INF/classes/com/jilinjobs/cms/advertisement/AdminAdvertisementController.class",
            "BOOT-INF/classes/com/jilinjobs/cms/listing/AdminCmsListController.class",
            "BOOT-INF/classes/com/jilinjobs/cms/siteconfig/AdminSiteConfigController.class",
            "BOOT-INF/classes/com/jilinjobs/cms/staticresource/AdminStaticResourceController.class",
        )
        require(forbiddenServerClasses.none(migrationEntries::contains)) { "Migration BootJar contains Server transport classes" }
        println("EU46_BACKEND_APPLICATION_BOUNDARY_VERIFY PASS")
    }
}
