import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.9.25"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
    id("groovy")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.4"
    id("com.google.cloud.tools.jib") version "2.8.0"
    id("io.micronaut.test-resources") version "4.4.4"
    id("io.micronaut.aot") version "4.4.4"
    id("com.github.node-gradle.node") version "7.0.2"
}

version = "0.0.1"
group = "lol.pbu.pricemiser"

val kotlinVersion = project.properties["kotlinVersion"]
val frontendRootDir = File("${project.projectDir}/src/main/frontend")

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.security:micronaut-security-annotations")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.views:micronaut-views-fieldset")
    implementation("io.micronaut.views:micronaut-views-thymeleaf")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("io.micronaut.views:micronaut-views-thymeleaf")
    implementation("io.micronaut.views:micronaut-views-htmx")
    compileOnly("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("com.amazonaws:aws-java-sdk-core")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("org.testcontainers:localstack")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:spock")
    testImplementation("org.testcontainers:testcontainers")
}

application {
    mainClass = "lol.pbu.pricemiser.ApplicationKt"
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("spock2")
    processing {
        incremental(true)
        annotations("lol.pbu.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}

// front end crud

node {
    version.set("18.17.1")
    npmVersion.set((project.properties["npmVersion"] as String))
    npmInstallCommand.set("install")
    distBaseUrl.set("https://nodejs.org/dist")
    download.set(false)
    workDir.set(file("${frontendRootDir}/.cache/nodejs"))
    npmWorkDir.set(file("${frontendRootDir}/.cache/npm"))
    nodeProjectDir.set(file(frontendRootDir))
    nodeProxySettings.set(ProxySettings.SMART)
}

val buildTaskUsingNpm = tasks.register<NpmTask>("buildNpm") {
    val frontEndBuildDir = File("${project.buildDir}/src/main/frontend")
    description = "Run npm build --out-dir ${frontEndBuildDir}/"
    group = "lol.pbu.pricemiser"
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    args.set(listOf("--", "--out-dir", "${frontEndBuildDir}/"))
    inputs.dir("src")
    outputs.dir("${frontEndBuildDir}/")
}




