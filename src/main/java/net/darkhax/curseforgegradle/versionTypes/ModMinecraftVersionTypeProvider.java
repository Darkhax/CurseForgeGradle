package net.darkhax.curseforgegradle.versionTypes;

import net.darkhax.curseforgegradle.api.versions.VersionType;

import java.util.HashSet;
import java.util.Set;

/**
 * A version type provider for Minecraft Versions in the mod category.
 */
public class ModMinecraftVersionTypeProvider implements VersionTypeProvider {
    @Override
    public Set<Long> getValidVersionTypes(VersionType[] versionTypes) {
        final Set<Long> validVersionTypes = new HashSet<>();

        for (final VersionType type : versionTypes) {
            if (type.getSlug().startsWith("minecraft")) {
                validVersionTypes.add(type.getId());
            }
        }

        return validVersionTypes;
    }
}
