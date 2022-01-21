package net.darkhax.curseforgegradle;

import com.google.common.collect.ImmutableList;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is responsible for detecting versions from properties in the build environment. Each task will have one
 * instance of this class associated to it.
 */
public final class VersionDetector {

    /**
     * The project that this detector is attached to. This is used for finding applied plugins or properties.
     */
    private final Project project;

    /**
     * The debug logger for the detector. When a new version is detected it will be logged to help debug things in the
     * future.
     */
    private final Logger log;

    /**
     * A set of detected versions. Only read from this set using {@link #getDetectedVersions()}.
     */
    private final Set<String> detectedVersions = new HashSet<>();

    /**
     * A flag that determines if the auto-detection is enabled. This can be disabled with user configs.
     */
    public boolean isEnabled = true;

    /**
     * The version detector should not be constructed manually. It is automatically constructed when the CurseForge
     * publish task is defined. Each task will have its own instance of the version detector.
     *
     * @param project The project associated with this version detector. This is primarily used to check applied plugins
     *                and build properties.
     * @param log     The log output for debug information. This is taken from the task that owns this instance.
     */
    VersionDetector(Project project, Logger log) {

        this.project = project;
        this.log = log;
    }

    /**
     * Initiates the detection of game versions. If {@link #isEnabled} is false this will not run.
     */
    public void detectVersions() {

        if (isEnabled) {

            // Minecraft support
            detectPlugin("net.minecraftforge.gradle", "Forge");
            detectPlugin("fabric-loom", "Fabric");
            detectProperty("MC_VERSION");
            detectProperty("minecraft_version");
            detectProperty("mc_version");
            detectProperty("mcVersion");
            detectProperty("minecraftVersion");
        }
    }

    /**
     * Gets an immutable collection of all the detected game versions.
     *
     * @return An immutable collection of all the detected game versions.
     */
    public Collection<String> getDetectedVersions() {

        return ImmutableList.copyOf(this.detectedVersions);
    }

    /**
     * Applies a given game version if a Gradle plugin is applied within the same script.
     *
     * @param pluginName The name of the plugin to search for. This should be the same as the name used to apply the
     *                   plugin.
     * @param version    The version to apply if the plugin is detected.
     */
    private void detectPlugin(String pluginName, String version) {

        if (project.getPlugins().hasPlugin(pluginName)) {

            this.log.debug("Detected plugin '{}'. Automatically applying version '{}'.", pluginName, version);
            this.detectedVersions.add(version);
        }
    }

    /**
     * Applies a given property as a game version.
     *
     * @param propertyName The property name to read as a version.
     */
    private void detectProperty(String propertyName) {

        final String propertyVersion = TaskPublishCurseForge.parseString(project.findProperty(propertyName));

        if (propertyVersion != null) {

            this.log.debug("Detected property '{}'. Automatically applying version '{}'.", propertyName, propertyVersion);
            this.detectedVersions.add(propertyVersion);
        }
    }
}