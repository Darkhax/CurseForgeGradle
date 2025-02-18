package net.darkhax.curseforgegradle.versionTypes;

import net.darkhax.curseforgegradle.api.versions.VersionType;

import java.util.Set;

public interface VersionTypeProvider {
    /**
     * Get a list of all valid game version type ids that should be considered when finding the ID of a version
     * @param versionTypes all available version types fetched from the API
     * @return a set of all valid version type ids
     */
    Set<Long> getValidVersionTypes(VersionType[] versionTypes);
}
