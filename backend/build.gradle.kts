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
val jilinjobsSitePackageRoot = file("../sites/jilinjobs").absolutePath

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

tasks.register<JavaExec>("importPartyHistoricalContent") {
    group = "migration"
    description = "Import the canonical Party historical-content dataset, including EU-30 theme education"
    dependsOn(":apps:content-migration:classes")
    classpath = migrationSourceSets.getByName("main").runtimeClasspath
    mainClass.set("com.jilinjobs.cms.migration.PartyHistoricalContentMigrationV2Kt")
    environment("CMS_SITE_PACKAGE_ROOT", jilinjobsSitePackageRoot)
}

tasks.register<JavaExec>("importPartyCarousel") {
    group = "migration"
    description = "Import the Party carousel canonical dataset with LINK / ARTICLE placement resolution"
    dependsOn(":apps:content-migration:classes")
    classpath = migrationSourceSets.getByName("main").runtimeClasspath
    mainClass.set("com.jilinjobs.cms.migration.PartyCarouselMigrationV2Kt")
    environment("CMS_SITE_PACKAGE_ROOT", jilinjobsSitePackageRoot)
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

tasks.register<JavaExec>("verifyContentMigrationBoundary") {
    group = "verification"
    description = "Verify Content Migration is non-web, excludes Server transport and composes Core/Flyway/Party capabilities"
    dependsOn(":apps:content-migration:testClasses")
    classpath = migrationSourceSets.getByName("test").runtimeClasspath
    mainClass.set("com.jilinjobs.cms.ContentMigrationBoundaryVerificationKt")
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
        require(migrationEntries.any { it.startsWith("BOOT-INF/classes/com/jilinjobs/cms/migration/PartyHistoricalContentMigrationV2") }) {
            "Migration BootJar is missing Party migration classes"
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
