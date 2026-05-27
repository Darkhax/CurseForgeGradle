# CurseForgeGradle ![Latest Version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/net/darkhax/curseforgegradle/net.darkhax.curseforgegradle.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=Latest%20Version)

CurseForgeGradle is a plugin for Gradle that allows you to publish build
artifacts to CurseForge.

## Features

- Upload directly to CurseForge using the official upload API.
- Auto-detect Minecraft game version and mod loader.
- Task based publishing allows advanced scripting and configuration.
- Upload to multiple CurseForge projects at once.
- Supports all games on CurseForge!

## Usage Guide

### Getting the plugin

This plugin is hosted on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/net.darkhax.curseforgegradle).

You can add the plugin to your Gradle project using the plugins DSL.

```groovy
plugins {
  id("net.darkhax.curseforgegradle") version "plugin_version_here"
}
```

### Basic Usage

This plugin allows you to define new tasks that upload files to CurseForge. The
following example shows how a Java based project would upload their main JAR.

```groovy
task publishCurseForge(type: net.darkhax.curseforgegradle.TaskPublishCurseForge) {
    
    // Your auth token for CurseForge. This is like a password, and you should
    // never share your token with a third party. Store this token in an 
    // environment variable or build secret.
    apiToken = findProperty('curseforge_token')
    
    // The ID of the CurseForge project you want to upload to.
    def projectId = findProperty('curseforge_project')
    
    // Defines the output of the jar task as the file to upload. This returns
    // an UploadArtifact that can be used to configure things like versions and
    // the changelog.
    def mainFile = upload(projectId, jar)
    mainFile.changelog = 'The changelog string for this file.'
    mainFile.addGameVersion('1.21.1')
}
```

### Automatic Version Detection

In some cases CurseForgeGradle will be able to detect versions from context
clues in your build environment. This can be disabled with 
`disableVersionDetection()`

#### Minecraft

**Mod Loaders**
The mod loader will be automatically set if a relevant plugin is detected.

| Plugin ID                     | Platform   |
|-------------------------------|------------|
| `net.minecraftforge.gradle`   | `Forge`    |
| `fabric-loom`                 | `Fabric`   |
| `org.quiltmc.loom`            | `Quilt`    |
| `net.neoforged.gradle`        | `NeoForge` |
| `net.neoforged.gradle.userdev`| `NeoForge` |
| `net.neoforged.moddev`        | `NeoForge` |

**Game Version**
If any of the following properties are set, it will be used as the Minecraft
version.

- `MC_VERSION`
- `minecraft_version`
- `mc_version`
- `mcVersion`
- `minecraftVersion`

**Java Version**
If the java language version has been set in Gradle, it will be used as the
Minecraft java version.

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

**Environment**
Starting **July 15th 2026** CurseForge will require all Minecraft mods to
define environment tags. By default CurseForgeGradle will set both the Client
and Server tag. This will only happen if you do not define a target environment
on your own.

### Configurable Properties

#### TaskPublishCurseForge
| Name                      | Accepted Type          | Description                                                                                                                                                                                                                                                                                                 |
|---------------------------|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| apiToken                  | String\|File\|Closure  | The API token used to authenticate with CurseForge. Setting this property is required to use this plugin.                                                                                                                                                                                                   |
| apiEndpoint               | String\|File\|Closure  | The API endpoint to upload the file to. This is the legacy CF API by default, which supports all games on CurseForge.                                                                                                                                                                                       |
| debugMode                 | Boolean                | Determines if publishing should actually happen or if the request should just be logged instead. This is an optional property and will default to false.                                                                                                                                                    |
| upload(projectId, file)   | String\|Number, Object | Defines a file to upload. The projectId can be a valid numeric String or any valid number. The file can be a file reference, an AbstractArchiveTask, or any other object Gradle can resolve as a file. This returns an UploadArtifact object which can be used to configure the file before publishing it.  |
| disableVersionDetection() |                        | Invoking this method will disable automatic version detection for all files uploaded by this instance of the task.                                                                                                                                                                                          |

#### UploadArtifact
| Name                            | Accepted Type                                     | Description                                                                                                                                                                                |
|---------------------------------|---------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| changelog                       | String\|File\|Closure                             | The changelog for the file. This is optional.                                                                                                                                              |
| changelogType                   | String\|File\|Closure                             | The formatting type of the changelog. The default is plaintext but html and markdown are also accepted.                                                                                    |
| displayName                     | String\|File\|Closure                             | An optional display name that will visually replace the file name. Using this method is often discouraged.                                                                                 |
| releaseType                     | String\|File\|Closure                             | The type of release you are publishing. This accepts alpha, beta, and release. The default is alpha.                                                                                       |
| addIncompatibility(slugs...)    | String\|File\|Closure, ...                        | Marks the file as being incompatible with the specified project(s).                                                                                                                        |
| addRequirement(slugs...)        | String\|File\|Closure, ...                        | Marks the file as requiring a file from the specified project(s).                                                                                                                          |
| addEmbedded(slugs...)           | String\|File\|Closure, ...                        | Marks the file as containing an embedded implementation of another project(s).                                                                                                             |
| addTool(slugs...)               | String\|File\|Closure, ...                        | Marks the file as having a tool relation with the specified project(s).                                                                                                                    |
| addOptional(slugs...)           | String\|File\|Closure, ...                        | Marks the file as having an optional dependency on the specified project(s).                                                                                                               |
| addModLoader(modloaders...)     | String\|File\|Closure, ...                        | Adds one or multiple mod loader tags to the file. Known accepted values include Forge, Fabric, and Rift.                                                                                   |
| addEnvironment(environments...) | String\|File\|Closure, ...                        | Adds one or more environments that the file can run on. Accepted values include Client and Server.                                                                                         |
| addJavaVersion(versions...)     | String\|File\|Closure, ...                        | Marks the file as being compatible with the given Java version(s).                                                                                                                         |
| addGameVersion(versions...)     | String\|File\|Closure, ...                        | Adds one or multiple game version to the file. This can only be used on parent files.                                                                                                      |
| addRelation(slug, type)         | String\|File\|Closure, String\|File\|Closure      | Adds a relationship between the file and another project.                                                                                                                                  |
| addRelations(type, slugs...)    | String\|File\|Closure, String\|File\|Closure, ... | Adds a relationship between the file and multiple other project. Note: The parameters are in a different order than for addRelation                                                        |
| withAdditionalFile(file)        | Object                                            | Creates a new UploadArtifact that will be uploaded as an additional/sub/child file. The provided object can be a file, ArchiveUploadTask, or any other value Gradle can resolve to a file. |