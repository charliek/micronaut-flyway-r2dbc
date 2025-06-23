# Release Process

!!! info "For Maintainers"
    This guide is for project maintainers who have permission to create releases.

## Overview

Releases are automated through GitHub Actions:
- **Snapshot releases**: Automatically published on every push to `main`
- **Official releases**: Triggered by creating tags starting with `v`

## Release Types

### Snapshot Releases

Snapshots are automatically published when changes are pushed to `main`:

```bash
# After merging a PR to main
# A snapshot is automatically published as:
# version-SNAPSHOT (e.g., 0.0.2-SNAPSHOT)
```

Users can consume snapshots:
```kotlin
dependencies {
    implementation("us.charliek:micronaut-flyway-r2dbc:0.0.2-SNAPSHOT")
}
```

### Official Releases

Official releases are created by tagging:

```bash
# Create and push a release tag
git tag v0.0.2
git push origin v0.0.2
```

This triggers the release workflow which:
1. Builds the project
2. Runs all tests
3. Publishes to GitHub Packages
4. Creates a GitHub Release

## Release Checklist

### Before Release

- [ ] All tests pass on `main`
- [ ] Documentation is up to date
- [ ] CHANGELOG is updated
- [ ] Version in `build.gradle.kts` is correct

### Create Release

1. **Update version** in `build.gradle.kts`:
   ```kotlin
   version = "0.0.2"
   ```

2. **Update CHANGELOG.md**:
   ```markdown
   ## [0.0.2] - 2023-12-25
   
   ### Added
   - MySQL R2DBC URL converter support
   
   ### Fixed
   - Connection retry logic for transient failures
   ```

3. **Commit changes**:
   ```bash
   git add build.gradle.kts CHANGELOG.md
   git commit -m "chore: prepare release 0.0.2"
   git push origin main
   ```

4. **Create and push tag**:
   ```bash
   git tag v0.0.2
   git push origin v0.0.2
   ```

5. **Verify release**:
   - Check GitHub Actions for successful build
   - Verify package appears in GitHub Packages
   - Check GitHub Releases page

### After Release

1. **Update version** for next development:
   ```kotlin
   version = "0.0.3"
   ```

2. **Commit**:
   ```bash
   git add build.gradle.kts
   git commit -m "chore: prepare for next development iteration"
   git push origin main
   ```

## Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR.MINOR.PATCH** (e.g., 1.2.3)
- **MAJOR**: Breaking API changes
- **MINOR**: New features, backwards compatible
- **PATCH**: Bug fixes, backwards compatible

During early development (0.x.x):
- API may change between minor versions
- Use exact version pinning in dependencies

## GitHub Actions Workflows

### CI Workflow (.github/workflows/ci.yml)

Runs on:
- Push to `main` or `develop`
- Pull requests

Actions:
- Build project
- Run tests
- Validate code

### Snapshot Workflow (.github/workflows/snapshot.yml)

Runs on:
- Push to `main`

Actions:
- Build project
- Run tests  
- Publish snapshot to GitHub Packages

### Release Workflow (.github/workflows/release.yml)

Runs on:
- Tags starting with `v`

Actions:
- Build project
- Run tests
- Publish release to GitHub Packages
- Create GitHub Release

## Troubleshooting Releases

### Release Build Fails

1. Check GitHub Actions logs
2. Ensure tag format is correct (`v0.0.2`)
3. Verify `build.gradle.kts` version matches tag

### Package Not Visible

1. Check package visibility settings
2. Ensure authentication is configured
3. Wait a few minutes for propagation

### Snapshot Not Updating

1. Check snapshot workflow succeeded
2. Clear local Gradle cache:
   ```bash
   rm -rf ~/.gradle/caches/modules-2/files-2.1/us.charliek/micronaut-flyway-r2dbc
   ```
3. Force refresh:
   ```bash
   ./gradlew build --refresh-dependencies
   ```

## Manual Release (Emergency)

If automation fails, publish manually:

```bash
# Set credentials
export GITHUB_USERNAME=your-username
export GITHUB_TOKEN=your-token

# Publish
./gradlew publish
```

## Release Notes Template

```markdown
## What's Changed

### ✨ New Features
- Add MySQL R2DBC URL converter (#123) by @contributor

### 🐛 Bug Fixes  
- Fix connection retry logic (#124) by @contributor

### 📚 Documentation
- Update configuration examples (#125) by @contributor

### 🏗️ Dependencies
- Update Micronaut to 4.2.0 (#126)

**Full Changelog**: https://github.com/charliek/micronaut-flyway-r2dbc/compare/v0.0.1...v0.0.2
```

## Security Releases

For security fixes:

1. Do not disclose details in public commits
2. Create release normally
3. Add security notice to release notes after release
4. Consider yanking affected versions

## Deprecation Process

When deprecating features:

1. Add `@Deprecated` annotation with explanation
2. Update documentation with deprecation notice
3. Provide migration guide
4. Remove in next major version

Example:
```kotlin
@Deprecated(
    message = "Use FlywayR2dbcConfigurationProperties instead",
    replaceWith = ReplaceWith("FlywayR2dbcConfigurationProperties"),
    level = DeprecationLevel.WARNING
)
class OldConfiguration
```

## Next Steps

- [How It Works](../core-concepts/how-it-works.md) - Understand how the library operates
- [Configuration Guide](../getting-started/configuration.md) - Configure the library