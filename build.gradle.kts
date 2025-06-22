import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("kapt") version "1.9.23"
    id("maven-publish")
    id("signing")
    id("jacoco")
}

group = "us.charliek"

version =
    if (project.hasProperty("snapshot") && project.property("snapshot") == "true") {
        "0.0.1-SNAPSHOT"
    } else {
        "0.0.1"
    }

repositories { mavenCentral() }

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    // Micronaut Core
    implementation("io.micronaut:micronaut-runtime:4.8.3")
    implementation("io.micronaut:micronaut-context:4.8.3")
    kapt("io.micronaut:micronaut-inject-java:4.8.3")

    // Flyway
    implementation("org.flywaydb:flyway-core:11.9.2")
    implementation("org.flywaydb:flyway-database-postgresql:11.9.2")

    // Connection Pooling
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.12")

    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // Testing
    testImplementation("io.micronaut.test:micronaut-test-junit5:4.5.0")
    testImplementation("org.testcontainers:postgresql:1.19.7")
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")

    // Test runtime dependencies
    testRuntimeOnly("org.postgresql:postgresql:42.7.3")
    testRuntimeOnly("org.postgresql:r2dbc-postgresql:1.0.5.RELEASE")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.3")

    // Micronaut annotation processing for tests
    kaptTest("io.micronaut:micronaut-inject-java:4.8.3")
    testImplementation("io.micronaut:micronaut-inject:4.8.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        javaParameters = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStackTraces = true
    }
}

jacoco { toolVersion = "0.8.11" }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "us.charliek"
            artifactId = "micronaut-flyway-r2dbc"
            version = project.version.toString()

            pom {
                name.set("Micronaut Flyway R2DBC")
                description.set(
                    "Flyway database migrations for Micronaut R2DBC applications without dual datasource complexity"
                )
                url.set("https://github.com/charliek/micronaut-flyway-r2dbc")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("charliek")
                        name.set("Charlie Knudsen")
                        email.set("charlie.knudsen@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/charliek/micronaut-flyway-r2dbc.git")
                    developerConnection.set(
                        "scm:git:ssh://github.com/charliek/micronaut-flyway-r2dbc.git"
                    )
                    url.set("https://github.com/charliek/micronaut-flyway-r2dbc")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/charliek/micronaut-flyway-r2dbc")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
