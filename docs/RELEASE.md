# Release Process

This document describes how to release a new version of micronaut-flyway-r2dbc.

## Prerequisites

- Ensure you have push access to the main repository
- Ensure all tests are passing on the main branch
- Ensure the CHANGELOG.md is updated with the new version's changes

## Versioning

This project follows [Semantic Versioning](https://semver.org/):
- MAJOR version for incompatible API changes
- MINOR version for backwards-compatible functionality additions  
- PATCH version for backwards-compatible bug fixes

## Release Steps

### 1. Update Version

Update the version in `build.gradle.kts`:

```kotlin
version = if (project.hasProperty("snapshot") && project.property("snapshot") == "true") {
    "X.Y.Z-SNAPSHOT"  // Update this
} else {
    "X.Y.Z"           // Update this
}
```

### 2. Update Documentation

Update the version in documentation files:
- `README.md` - Update the dependency version in the usage section
- `CHANGELOG.md` - Add a new section for the release with the date

### 3. Commit Changes

```bash
git add .
git commit -m "Release version X.Y.Z"
git push origin main
```

### 4. Create and Push Tag

Create a tag for the release:

```bash
git tag vX.Y.Z
git push origin vX.Y.Z
```

The tag MUST start with 'v' to trigger the release workflow.

### 5. Verify Release

The GitHub Actions workflow will automatically:
1. Build and test the project
2. Publish to GitHub Packages
3. Create a GitHub release

Monitor the Actions tab to ensure the release completes successfully.

### 6. Post-Release

After a successful release, update to the next development version:

1. Update `build.gradle.kts` to the next version (e.g., `0.0.2` after releasing `0.0.1`)
2. Commit with message: `Prepare for next development iteration`
3. Push to main

## Snapshot Releases

Snapshot releases are automatically published when pushing to the main branch. These are useful for:
- Testing unreleased features
- Early access to bug fixes
- Integration testing

To use a snapshot version in your project:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/charliek/micronaut-flyway-r2dbc")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("us.charliek:micronaut-flyway-r2dbc:0.0.1-SNAPSHOT")
}
```

## GitHub Packages Notes

### Authentication

To consume packages from GitHub Packages, users need to authenticate:

1. Create a Personal Access Token with `read:packages` scope
2. Configure credentials in `~/.gradle/gradle.properties`:
   ```
   gpr.user=YOUR_GITHUB_USERNAME
   gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
   ```

### Snapshot Support

Yes, GitHub Packages fully supports SNAPSHOT versions. Benefits:
- Automatic versioning for development builds
- Easy testing of pre-release features
- No version conflicts with releases

Downsides:
- Requires authentication even for public packages
- Limited retention (snapshots may be cleaned up)
- Not available in Maven Central

## Troubleshooting

### Release Workflow Fails

1. Check GitHub Actions logs for specific errors
2. Ensure the tag format is correct (must start with 'v')
3. Verify GitHub token permissions

### Package Not Available

1. Check that the release workflow completed successfully
2. Verify authentication credentials are correct
3. Ensure the repository URL in your build file is correct

## Future Considerations

### Maven Central Publishing

Consider publishing to Maven Central for broader accessibility:
- No authentication required for consumers
- Better discoverability
- Integration with dependency management tools

This would require:
- Sonatype OSSRH account
- GPG signing setup
- Additional publishing configuration