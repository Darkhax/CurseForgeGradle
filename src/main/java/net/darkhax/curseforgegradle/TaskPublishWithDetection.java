package net.darkhax.curseforgegradle;

/**
 * A Gradle task that can publish multiple files to CurseForge. A project can define any number of these tasks, and any
 * given task can be responsible for publishing any number of files to any number of projects.
 *
 * <p>
 * Unlike the parent {@link TaskPublishCurseForge}, this task auto-detects versioning information for the current project. This task also
 * does not currently support <a href="https://docs.gradle.org/current/userguide/configuration_cache.html">Gradle's configuration cache</a> system.
 * </p>
 */
public abstract class TaskPublishWithDetection extends TaskPublishCurseForge {

    /**
     * Handles the automatic discovery of game version tags from variables in the Gradle environment. If this is not
     * disabled detected versions will be applied in {@link #initialize()}.
     */
    private final VersionDetector versionDetector;

    /**
     * This task should not be constructed manually. It will be constructed dynamically by Gradle when a user defines
     * the task. Code inside the constructor will be executed before the user configuration.
     */
    public TaskPublishWithDetection() {

        this.versionDetector = new VersionDetector(this.getProject(), this.log);
        //TODO: Eventually try to figure out how to make the Version detector support the configuration cache
        // and then remove this task merging the optional version detection back into the base task type
        notCompatibleWithConfigurationCache("Version detection does not currently support the configuration cache");
    }

    /**
     * Validates the task configuration and sets up data required for publishing artifacts.
     */
    @Override
    protected void initialize() {

        super.initialize();

        // Handle auto version detection.
        this.versionDetector.detectVersions(this.validGameVersions);

        for (String detectedVersion : this.versionDetector.getDetectedVersions()) {

            for (UploadArtifact artifact : getUploadArtifacts().get()) {

                artifact.addGameVersion(detectedVersion);
            }
        }
    }
}