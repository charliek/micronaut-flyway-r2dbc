# Installation

!!! warning "Early Development"
    This library is in early development. APIs and functionality may change between releases.

## Prerequisites

- JDK 21 or higher
- Micronaut 4.x
- An R2DBC-compatible database (currently PostgreSQL is fully supported)
- Docker (for running tests with TestContainers)

## Gradle Setup

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/charliek/micronaut-flyway-r2dbc")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("us.charliek:micronaut-flyway-r2dbc:0.0.1")
    
    // Your R2DBC driver
    implementation("io.r2dbc:r2dbc-postgresql:1.0.4.RELEASE")
    
    // JDBC driver for migrations (must match your R2DBC database)
    runtimeOnly("org.postgresql:postgresql:42.7.3")
}
```

## GitHub Packages Authentication

!!! info "Authentication Required"
    GitHub Packages requires authentication for all packages, even from public repositories. This is a GitHub limitation.

### Option 1: Gradle Properties (Recommended)

Create or edit `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
```

### Option 2: Environment Variables

Set these environment variables:

```bash
export USERNAME=YOUR_GITHUB_USERNAME
export TOKEN=YOUR_PERSONAL_ACCESS_TOKEN
```

### Creating a Personal Access Token

1. Go to [GitHub Settings → Developer settings → Personal access tokens](https://github.com/settings/tokens)
2. Click "Generate new token (classic)"
3. Give it a descriptive name
4. Select the `read:packages` scope
5. Click "Generate token"
6. Copy the token immediately (you won't see it again)

## Maven Setup

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/charliek/micronaut-flyway-r2dbc</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>us.charliek</groupId>
        <artifactId>micronaut-flyway-r2dbc</artifactId>
        <version>0.0.1</version>
    </dependency>
    
    <!-- R2DBC driver -->
    <dependency>
        <groupId>io.r2dbc</groupId>
        <artifactId>r2dbc-postgresql</artifactId>
        <version>1.0.4.RELEASE</version>
    </dependency>
    
    <!-- JDBC driver for migrations -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.3</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

Configure authentication in `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
</servers>
```

## Next Steps

Once installed, proceed to the [Quick Start Guide](quick-start.md) to run your first migration.