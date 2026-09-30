import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

val mainSourceSet = sourceSets.getByName("main")
val reviewSourceSet = sourceSets.create("review") {
    compileClasspath += mainSourceSet.output
    runtimeClasspath += mainSourceSet.output
}

configurations[reviewSourceSet.implementationConfigurationName].extendsFrom(configurations["implementation"])
configurations[reviewSourceSet.runtimeOnlyConfigurationName].extendsFrom(configurations["runtimeOnly"])

val reviewTestSourceSet = sourceSets.create("reviewTest") {
    compileClasspath += mainSourceSet.output + reviewSourceSet.output
    runtimeClasspath += mainSourceSet.output + reviewSourceSet.output
}
configurations[reviewTestSourceSet.implementationConfigurationName].extendsFrom(configurations["testImplementation"])
configurations[reviewTestSourceSet.runtimeOnlyConfigurationName].extendsFrom(configurations["testRuntimeOnly"])

dependencies {
    implementation(project(":modules:cms-core"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.aspectj:aspectjweaver")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.flywaydb:flyway-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework:spring-jdbc")
    testImplementation("org.flywaydb:flyway-core")
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

tasks.register<BootJar>("reviewBootJar") {
    group = "build"
    description = "Build the isolated Local CI / Human Review Server with test-only identity adapter"
    dependsOn(reviewSourceSet.classesTaskName)
    archiveFileName.set("jilinjobs-cms-backend-review-0.1.0-SNAPSHOT.jar")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    mainClass.set("com.jilinjobs.cms.CmsApplicationKt")
    targetJavaVersion.set(JavaVersion.VERSION_21)
    classpath(reviewSourceSet.runtimeClasspath)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Test>("reviewTest") {
    group = "verification"
    description = "Verify isolated Review identity session lifecycle without exposing it to formal tests or artifacts"
    testClassesDirs = reviewTestSourceSet.output.classesDirs
    classpath = reviewTestSourceSet.runtimeClasspath
}
