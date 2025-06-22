plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("kapt") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.2"
}

group = "us.charliek.examples"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    kapt("io.micronaut:micronaut-inject-java")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.r2dbc:micronaut-r2dbc")
    implementation("io.micronaut.data:micronaut-data-r2dbc")
    
    // Add the library from parent project
    implementation(project(":"))
    
    // Database drivers
    runtimeOnly("org.postgresql:postgresql:42.7.3")
    runtimeOnly("org.postgresql:r2dbc-postgresql:1.0.5.RELEASE")
    
    // Logging
    runtimeOnly("ch.qos.logback:logback-classic")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
}