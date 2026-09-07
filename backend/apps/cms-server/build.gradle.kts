plugins {
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
}

dependencies {
    implementation(project(":modules:cms-core"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
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
    mainClass.set("com.jilinjobs.cms.CmsApplicationKt")
}

tasks.bootJar {
    archiveFileName.set("jilinjobs-cms-backend-0.1.0-SNAPSHOT.jar")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
