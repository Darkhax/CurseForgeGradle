# CurseForgeGradle ![Latest Version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/net/darkhax/curseforgegradle/net.darkhax.curseforgegradle.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=Latest%20Version)

A Gradle plugin for publishing Gradle build artifacts or other files directly to CurseForge. This project is community made and is not officially endorsed by CurseForge or Overwolf.

## Features
- Upload tasks directly to CurseForge using their [official upload API](https://support.curseforge.com/en/support/solutions/articles/9000197321-curseforge-api).
- Auto-detect game version and mod loader based on build environment context clues.
- Task based publishing that allows for a wide range of scripting and configuration options.
- Upload to more than one CurseForge project.

## Usage Guide
This section will explain where to download this plugin, how it works, and all the available configuration properties. 

### Where to Download
This Gradle plugin is available from the official Gradle plugin repository. Both the Plugin DSL and legacy plugin applications are supported.

#### Plugin DSL
```groovy
plugins {
    id 'net.darkhax.curseforgegradle' version 'version_here'
}
```

#### Legacy Plugin Application
```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath group: 'gradle.plugin.net.darkhax.curseforgegradle', name: 'CurseForgeGradle', version: 'version_here'
    }
}
```

### Basic Configuration
This plugin uses a task based approach to uploading files. Projects define a new task in their build script that will publish various files when invoked. The following example demonstrates how a Java based project would upload their main JAR.

```groovy
task publishCurseForge(type: net.darkhax.curseforgegradle.TaskPublishCurseForge) {

    // This token is used to authenticate with CurseForge. It should be handled
    // with the same level of care and security as your actual password. You 
    // should never share your token with an untrusted source or publish it
    // publicly to GitHub or embed it within a project. The best practice is to
    // store this token in an environment variable or a build secret.
    apiToken = findProperty('curseforge_token')
    
    // A project ID is required to tell CurseForge which project the uploaded
    // file belongs to. This is public on your project page and is not private
    // information.
    projectId = findProperty('curseforge_project')

    // Tells CurseForgeGradle to publish the output of the jar task. This will
    // return a UploadArtifact object that can be used to further configure the
    // file. 
    def mainFile = upload(projectId, jar)
    mainFile.changelog = 'The changelog string for this file.'
}
```

### Examples
Various examples for using CurseForgeGradle can be found [here](https://github.com/Darkhax/CurseForgeGradle/tree/main/examples/local_test_forge). 

### Automatic Version Detection
In some cases CurseForgeGradle will be able to automatically detect version tags from context clues in your build environment. This can be useful if you don't want to define them manually however the results may not be perfect. This can be disabled using `disableVersionDetection()` within the body of your CurseForgeGradle task.

#### Minecraft
The following versions are detected when using CurseForgeGradle in a Minecraft project.
- When the `net.minecraftforge.gradle` plugin is applied in the same script the `Forge` tag will be applied.
- When the `fabric-loom` plugin is applied in the same script the `Fabric` tag will be applied.
- When the `MC_VERSION`, `mc_version`, or `minecraft_version` property is set its value will be added as a game version.
- When a java toolchain is configured in the same script a corresponding java version tag will be added as a game version.

### Available Properties
The following properties and methods are exposed for use within your script.

#### TaskPublishCurseForge
| Name                      | Accepted Type          | Description                                                                                                                                                                                                                                                                                                                                                    |
|---------------------------|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| apiToken                  | String\|File\|Closure  | The API token used to authenticate with CurseForge. Setting this property is required to use this plugin.                                                                                                                                                                                                                                                      |
| apiEndpoint               | String\|File\|Closure  | The API endpoint to upload the file to. This is an optional property and will default to the Minecraft API.                                                                                                                                                                                                                                                    |
| debugMode                 | Boolean                | Determines if publishing should actually happen or if the request should just be logged instead. This is an optional property and will default to false.                                                                                                                                                                                                       |
| upload(projectId, file)   | String\|Number, Object | Invoking this method will configure the task to upload a given file to a given project. The projectId can be a valid numeric String or any valid number. The file can be a file reference, an AbstractArchiveTask, or any other object Gradle can resolve as a file. This returns an UploadArtifact object which can be used to configure the file before publishing it. |
| disableVersionDetection() |                        | Invoking this method will disable automatic version detection for all files uploaded by this instance of the task.                                                                                                                                                                                                                                             |

#### UploadArtifact
| Name                         | Accepted Type                                     | Description                                                                                                                                                                                |
|------------------------------|---------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| changelog                    | String\|File\|Closure                             | The changelog for the file. This is optional.                                                                                                                                              |
| changelogType                | String\|File\|Closure                             | The formatting type of the changelog. The default is plaintext but html and markdown are also accepted.                                                                                    |
| displayName                  | String\|File\|Closure                             | An optional display name that will visually replace the file name. Using this method is often discouraged.                                                                                 |
| releaseType                  | String\|File\|Closure                             | The type of release you are publishing. This accepts alpha, beta, and release. The default is alpha.                                                                                       |
| addIncompatibility(slugs...) | String\|File\|Closure, ...                        | Marks the file as being incompatible with the specified project(s).                                                                                                                        |
| addRequirement(slugs...)     | String\|File\|Closure, ...                        | Marks the file as requiring a file from the specified project(s).                                                                                                                          |
| addEmbedded(slugs...)        | String\|File\|Closure, ...                        | Marks the file as containing an embedded implementation of another project(s).                                                                                                             |
| addTool(slugs...)            | String\|File\|Closure, ...                        | Marks the file as having a tool relation with the specified project(s).                                                                                                                    |
| addOptional(slugs...)        | String\|File\|Closure, ...                        | Marks the file as having an optional dependency on the specified project(s).                                                                                                               |
| addModLoader(modloaders...)  | String\|File\|Closure, ...                        | Adds one or multiple mod loader tags to the file. Known accepted values include Forge, Fabric, and Rift.                                                                                   |
| addJavaVersion(versions...)  | String\|File\|Closure, ...                        | Marks the file as being compatible with the given Java version(s).                                                                                                                         |
| addGameVersion(versions...)  | String\|File\|Closure, ...                        | Adds one or multiple game version to the file. This can only be used on parent files.                                                                                                      |
| addRelation(slug, type)      | String\|File\|Closure, String\|File\|Closure      | Adds a relationship between the file and another project.                                                                                                                                  |
| addRelations(type, slugs...) | String\|File\|Closure, String\|File\|Closure, ... | Adds a relationship between the file and multiple other project. Note: The parameters are in a different order than for addRelation                                                        |
| withAdditionalFile(file)     | Object                                            | Creates a new UploadArtifact that will be uploaded as an additional/sub/child file. The provided object can be a file, ArchiveUploadTask, or any other value Gradle can resolve to a file. |

## Alternative Plugins
| Project                                                                             | Platform      | Description                                                                                                                                                                                                                                                                                                  |
|-------------------------------------------------------------------------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [CurseGradle](https://github.com/matthewprenger/CurseGradle)                        | Gradle        | An alternative Gradle plugin for uploading files to CurseForge developed in Groovy. This has been the trusted go to plugin for many years and still works great. CurseForgeGradle was developed as a direct alternative because I prefer explicitly defining tasks and have other minor design preferences.  |
| [Upload-To-CurseForge](https://github.com/marketplace/actions/upload-to-curseforge) | GitHub Action | A GitHub Action that allows uploading files to CurseForge. This option is perfect for those who prefer GitHub actions.                                                                                                                                                                                       |
