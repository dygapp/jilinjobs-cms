plugins {
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
}

dependencies {
    implementation(project(":modules:cms-core"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-web")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.0")
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
    mainClass.set("com.jilinjobs.cms.ContentMigrationApplicationKt")
}

tasks.bootJar {
    archiveFileName.set("jilinjobs-cms-content-migration-0.1.0-SNAPSHOT.jar")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
