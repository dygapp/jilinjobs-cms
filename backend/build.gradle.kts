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
    // EU-29 增加独立迁移 CLI 后，显式固定正常 Runtime 的 Spring Boot 入口，避免 bootJar 自动探测到多个 main class。
    mainClass.set("com.jilinjobs.cms.CmsApplicationKt")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JavaExec>("importPartyHistoricalContent") {
    group = "migration"
    description = "Import an EU-29 normalized Party historical-content snapshot"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.jilinjobs.cms.migration.PartyHistoricalContentMigrationKt")
}

tasks.register<JavaExec>("importPartyCarousel") {
    group = "migration"
    description = "Import the EU-29 normalized Party carousel snapshot"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.jilinjobs.cms.migration.PartyCarouselMigrationKt")
}
