package net.darkhax.curseforgegradle;

import com.google.common.collect.ImmutableList;
import net.darkhax.curseforgegradle.api.versions.GameVersions;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

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
     *
     * @param validGameVersions Valid game versions for the current game.
     */
    public void detectVersions(GameVersions validGameVersions) {

        if (isEnabled) {

            // Minecraft

            // Detect ModLoader versions.
            detectPlugin(validGameVersions, "net.minecraftforge.gradle", "Forge");
            detectPlugin(validGameVersions, "fabric-loom", "Fabric");
            detectPlugin(validGameVersions, "org.quiltmc.loom", "Quilt");
            detectPlugin(validGameVersions, "net.neoforged.gradle", "NeoForge");
            detectPlugin(validGameVersions, "net.neoforged.gradle.userdev", "NeoForge");
            detectPlugin(validGameVersions, "net.neoforged.moddev", "NeoForge");

            // Detect Minecraft versions.
            detectProperty(validGameVersions, "MC_VERSION");
            detectProperty(validGameVersions, "minecraft_version");
            detectProperty(validGameVersions, "mc_version");
            detectProperty(validGameVersions, "mcVersion");
            detectProperty(validGameVersions, "minecraftVersion");

            // Detect Java versions.
            detectJavaToolchainVersion(validGameVersions);
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
     * Adds a version as detected if it is a valid version for the current game.
     *
     * @param validGameVersions Valid game versions for the current game.
     * @param version           Version to add as detected.
     *
     * @return {@code true} if the game version was valid for the current game and was successfully added.
     */
    private boolean addDetectedChecked(GameVersions validGameVersions, String version) {

        if (validGameVersions.getVersion(version) != null) {

            return this.detectedVersions.add(version);
        }
        return false;
    }

    /**
     * Applies a given game version if a Gradle plugin is applied within the same script.
     *
     * @param pluginName The name of the plugin to search for. This should be the same as the name used to apply the
     *                   plugin.
     * @param version    The version to apply if the plugin is detected.
     */
    private void detectPlugin(GameVersions validGameVersions, String pluginName, String version) {

        if (project.getPlugins().hasPlugin(pluginName) && addDetectedChecked(validGameVersions, version)) {

            this.log.debug("Detected plugin '{}'. Automatically applying version '{}'.", pluginName, version);
        }
    }

    /**
     * Applies a given property as a game version.
     *
     * @param propertyName The property name to read as a version.
     */
    private void detectProperty(GameVersions validGameVersions, String propertyName) {

        final String propertyVersion = TaskPublishCurseForge.parseString(project.findProperty(propertyName));

        if (propertyVersion != null && addDetectedChecked(validGameVersions, propertyVersion)) {

            this.log.debug("Detected property '{}'. Automatically applying version '{}'.", propertyName, propertyVersion);
        }
    }

    /**
     * Attempts to detect the java version from the configured java tool chain.
     */
    private void detectJavaToolchainVersion(GameVersions validGameVersions) {

        JavaPluginExtension extension = this.project.getExtensions().findByType(JavaPluginExtension.class);

        if (extension !=  null) {

            Property<JavaLanguageVersion> languageVersion = extension.getToolchain().getLanguageVersion();
            int version = languageVersion.map(JavaLanguageVersion::asInt).getOrElse(0);

            if (version > 0 && addDetectedChecked(validGameVersions, "Java " + version)) {

                this.log.debug("Detected java version '{}'. Automatically applying version 'Java {}'.", version, version);
            }
        }
    }
}
