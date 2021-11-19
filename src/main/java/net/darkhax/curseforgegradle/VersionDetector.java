package net.darkhax.curseforgegradle;

import com.google.common.collect.ImmutableList;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class VersionDetector {

    private final Project project;
    private final Logger log;
    private final Set<String> detectedVersions = new HashSet<>();
    public boolean isEnabled = true;

    public VersionDetector(Project project, Logger log) {

        this.project = project;
        this.log = log;
    }

    public void detectVersions() {

        if (isEnabled) {

            // Minecraft support
            detectPlugin("net.minecraftforge.gradle", "Forge");
            detectPlugin("fabric-loom", "Fabric");
            detectProperty("MC_VERSION");
            detectProperty("minecraft_version");
            detectProperty("mc_version");
        }
    }

    public Collection<String> getDetectedVersions() {

        return ImmutableList.copyOf(this.detectedVersions);
    }

    private void detectPlugin(String pluginName, String version) {

        if (project.getPlugins().hasPlugin(pluginName)) {

            this.log.debug("Detected plugin '{}'. Automatically applying version '{}'.", pluginName, version);
            this.detectedVersions.add(version);
        }
    }

    private void detectProperty(String propertyName) {

        final String propertyVersion = TaskPublishCurseForge.parseString(project.findProperty(propertyName));

        if (propertyVersion != null) {

            this.log.debug("Detected property '{}'. Automatically applying version '{}'.", propertyName, propertyVersion);
            this.detectedVersions.add(propertyVersion);
        }
    }
}