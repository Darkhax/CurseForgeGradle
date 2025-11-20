package net.darkhax.curseforgegradle.versionTypes;

import net.darkhax.curseforgegradle.api.versions.VersionType;

import java.util.HashSet;
import java.util.Set;

/**
 * A version type provider for the Modloader option.
 */
public class ModloaderVersionTypeProvider implements VersionTypeProvider {
    @Override
    public Set<Long> getValidVersionTypes(VersionType[] versionTypes) {
        final Set<Long> validVersionTypes = new HashSet<>();

        for (final VersionType type : versionTypes) {
            if (type.getSlug().startsWith("minecraft") || type.getSlug().equals("java") ||
                    type.getSlug().equals("modloader") || type.getSlug().equals("environment")) {
                validVersionTypes.add(type.getId());
            }
        }

        return validVersionTypes;
    }
}
