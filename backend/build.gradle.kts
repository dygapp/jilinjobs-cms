plugins {
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
}

group = "com.jilinjobs"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.0")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260313.1")

    runtimeOnly("org.flywaydb:flyway-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property",
        )
    }
}

springBoot {
    // EU-29/EU-30 增加独立迁移 CLI 后，显式固定正常 Runtime 的 Spring Boot 入口，避免 bootJar 自动探测到多个 main class。
    mainClass.set("com.jilinjobs.cms.CmsApplicationKt")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val jilinjobsSitePackageRoot = file("../sites/jilinjobs").absolutePath

tasks.register<JavaExec>("importPartyHistoricalContent") {
    group = "migration"
    description = "Import the canonical Party historical-content dataset, including EU-30 theme education"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.jilinjobs.cms.migration.PartyHistoricalContentMigrationV2Kt")
    environment("CMS_SITE_PACKAGE_ROOT", jilinjobsSitePackageRoot)
}

tasks.register<JavaExec>("importPartyCarousel") {
    group = "migration"
    description = "Import the Party carousel canonical dataset with LINK / ARTICLE placement resolution"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.jilinjobs.cms.migration.PartyCarouselMigrationV2Kt")
    environment("CMS_SITE_PACKAGE_ROOT", jilinjobsSitePackageRoot)
}

tasks.register<JavaExec>("provisionSitePackage") {
    group = "provisioning"
    description = "Apply a versioned Site Package to an initialized CMS schema"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.jilinjobs.cms.provisioning.SitePackageProvisioningKt")
}

tasks.register<JavaExec>("verifySitePackageFoundation") {
    group = "verification"
    description = "Verify EU-37 Site Package provisioning against a real MySQL V1 generic schema"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.jilinjobs.cms.provisioning.SitePackageFoundationVerificationKt")
}

tasks.register<JavaExec>("verifyStableSiteStructure") {
    group = "verification"
    description = "Verify EU-38 stable Site Package structure against Fresh V1 and legacy V1+V2 databases"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.jilinjobs.cms.provisioning.StableSiteStructureVerificationKt")
}

tasks.register<JavaExec>("verifyRuntimeSitePackageComposition") {
    group = "verification"
    description = "Verify EU-40 opt-in Site Package runtime composition against the current V1+V2+V3 lifecycle"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.jilinjobs.cms.provisioning.RuntimeSitePackageCompositionVerificationKt")
}
