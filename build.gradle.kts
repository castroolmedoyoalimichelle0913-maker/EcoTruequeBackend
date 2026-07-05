plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    kotlin("plugin.serialization") version "2.3.21"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {

    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.compression)
    implementation(ktorLibs.server.cachingHeaders)

    implementation(libs.logback.classic)

    // JWT
    implementation("io.ktor:ktor-server-auth:3.5.0")
    implementation("io.ktor:ktor-server-auth-jwt:3.5.0")
    implementation("com.auth0:java-jwt:4.5.0")

    // BCrypt
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")

    // MySQL
    implementation("com.mysql:mysql-connector-j:9.4.0")

    // Pool
    implementation("com.zaxxer:HikariCP:6.3.0")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}