package net.darkhax.curseforgegradle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.darkhax.curseforgegradle.api.versions.GameVersions;
import org.apache.groovy.util.Maps;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.*;

import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/**
 * This class is responsible for detecting versions from properties in the build environment. Each task will have one
 * instance of this class associated to it.
 */
public final class VersionDetector {

    /**
     * A map of well known plugins and the versions they are associated with. This is used to automatically detect them.
     */
    private static final Map<String, String> WELL_KNOWN_PLUGINS = Maps.of(
            "net.minecraftforge.gradle", "Forge",
            "fabric-loom", "Fabric",
            "org.quiltmc.loom", "Quilt",
            "net.neoforged.gradle", "NeoForge",
            "net.neoforged.gradle.userdev", "NeoForge"
    );

    private static final Set<String> WELL_KNOWN_PROPERTIES = Sets.newHashSet(
            "MC_VERSION",
            "minecraft_version",
            "mc_version",
            "mcVersion",
            "minecraftVersion"
    );

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
     * A set of detected plugin versions. This detection is run lazily and when the task that owns this detector
     * initializes during task execution is it queried to register versions.
     */
    private final Map<String, String> detectedPluginVersions = new HashMap<>();

    /**
     * A set of detected properties. This detection is run lazily and when the task that owns this detector
     * initializes during task execution is it queried to register versions.
     */
    private final Map<String, Provider<String>> detectedProperties = new HashMap<>();

    /**
     * The version detector should not be constructed manually. It is automatically constructed when the CurseForge
     * publish task is defined. Each task will have its own instance of the version detector.
     *
     * @param project The project associated with this version detector. This is primarily used to check applied plugins
     *                and build properties.
     * @param log     The log output for debug information. This is taken from the task that owns this instance.
     */
    VersionDetector(Project project, Logger log) {
        this.log = log;

        //This operates as a lazy detection mechanism.
        //withId either executes immediately if the plugin is already applied or when the plugin is applied.
        //We then read the detected versions during task execution.
        WELL_KNOWN_PLUGINS.forEach((pluginName, version) -> {
            project.getPlugins().withId(pluginName, plugin -> {
                detectedPluginVersions.put(pluginName, version);
            });
        });

        //This operates as a lazy detection mechanism.
        //We then read the detected versions during task execution.
        WELL_KNOWN_PROPERTIES.forEach(propertyName -> {
            //This is a bit of a weird way to do it, but it is the only way to get the property value lazily.
            //We can't just use project.findProperty because it will throw an exception if the property is not found.
            //And we can not call it during task execution as we are not allowed to use or store the project.
            //So the best alternative is to use a provider that will lazily evaluate the property.
            //And if it is not currently registered on task creation, we use a gradle property provider instead.
            Provider<String> propertyProvider = project.hasProperty(propertyName) ?
                    project.provider(() -> TaskPublishCurseForge.parseString(project.findProperty(propertyName))) :
                    project.getProviders().gradleProperty(propertyName);

            propertyProvider = propertyProvider.orElse("");

            detectedProperties.put(propertyName, propertyProvider);
        });

        //Now we detect the java version from the java toolchain.
        JavaPluginExtension extension = project.getExtensions().findByType(JavaPluginExtension.class);

        if (extension !=  null) {
            //We use a lazy resolve here as the java toolchain is not always available.
            Property<JavaLanguageVersion> languageVersion = extension.getToolchain().getLanguageVersion();
            detectedProperties.put("JavaVersion", languageVersion
                            .map(JavaLanguageVersion::asInt)
                            .filter(version -> version > 0)
                            .map(version -> "Java " + version).orElse(""));
        }
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
            detectedPluginVersions.forEach((pluginName, version) -> {
                if (addDetectedChecked(validGameVersions, version)) {
                    this.log.debug("Detected plugin '{}'. Automatically applying version '{}'.", pluginName, version);
                }
            });

            // Detect properties (Which includes the java version)
            detectedProperties.forEach((propertyName, provider) -> {
                final String propertyValue = provider.get();
                if (!propertyValue.isEmpty() && addDetectedChecked(validGameVersions, propertyValue)) {
                    this.log.debug("Detected property '{}'. Automatically applying version '{}'.", propertyName, propertyValue);
                }
            });
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
}
