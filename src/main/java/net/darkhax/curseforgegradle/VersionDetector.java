package net.darkhax.curseforgegradle;

import com.google.common.collect.ImmutableList;
import org.gradle.api.Project;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class VersionDetector {

    private final Project project;
    private final Set<String> detectedVersions = new HashSet<>();
    public boolean isEnabled = true;

    public VersionDetector(Project project) {

        this.project = project;
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

            this.detectedVersions.add(version);
        }
    }

    private void detectProperty(String propertyName) {

        final String propertyVersion = TaskPublishCurseForge.parseString(project.findProperty(propertyName));

        if (propertyVersion != null) {

            this.detectedVersions.add(propertyVersion);
        }
    }
}