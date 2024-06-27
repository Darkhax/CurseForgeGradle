package net.darkhax.curseforgegradle;

//TODO: Eventually try to figure out how to make the Version detector support the configuration cache
// and then remove this task merging the optional version detection back into the base task type
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
        notCompatibleWithConfigurationCache("Version detection does not currently support the configuration cache");
    }

    /**
     * Disables automatic version detection for all artifacts published through the current task. Prefer just using TaskPublishCurseForge over calling this method.
     */
    public void disableVersionDetection() {

        this.versionDetector.isEnabled = false;
    }

    /**
     * Validates the task configuration and sets up data required for publishing artifacts.
     */
    @Override
    protected void initialize() {

        super.initialize();

        // Handle auto version detection.
        if (this.versionDetector.isEnabled) {

            this.versionDetector.detectVersions(this.validGameVersions);

            for (String detectedVersion : this.versionDetector.getDetectedVersions()) {

                for (UploadArtifact artifact : getUploadArtifacts().get()) {

                    artifact.addGameVersion(detectedVersion);
                }
            }
        }
    }
}