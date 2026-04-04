# Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.dnd/compose-dnd)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.dnd%22%20AND%20a:%22compose-dnd%22)

## Version Compatibility

| Kotlin version | Compose version | Compose DND version                 |
|----------------|-----------------|--------------------------------------|
| 2.3.20         | 1.10.3          | {{ compose_dnd_version }}            |

## Gradle Setup

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
implementation("com.mohamedrejeb.dnd:compose-dnd:{{ compose_dnd_version }}")
```

### Kotlin Multiplatform

For Kotlin Multiplatform projects, add the dependency to your `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mohamedrejeb.dnd:compose-dnd:{{ compose_dnd_version }}")
        }
    }
}
```

### Version Catalog

If you are using a version catalog (`libs.versions.toml`), add the following:

```toml
[versions]
compose-dnd = "{{ compose_dnd_version }}"

[libraries]
compose-dnd = { module = "com.mohamedrejeb.dnd:compose-dnd", version.ref = "compose-dnd" }
```

Then reference it in your `build.gradle.kts`:

```kotlin
implementation(libs.compose.dnd)
```

## Platform Notes

Compose DND supports all Compose Multiplatform targets:

- **Android** -- No additional setup required.
- **iOS** -- No additional setup required.
- **Desktop (JVM)** -- No additional setup required.
- **Web (JS)** -- No additional setup required.
- **Web (WASM)** -- No additional setup required.

!!! note
    Make sure your project uses a compatible version of Kotlin and Compose Multiplatform as listed in the version compatibility table above.
