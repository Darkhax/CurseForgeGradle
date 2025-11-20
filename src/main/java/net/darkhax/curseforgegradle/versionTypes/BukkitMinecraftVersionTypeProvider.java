package net.darkhax.curseforgegradle.versionTypes;

import net.darkhax.curseforgegradle.api.versions.VersionType;

import java.util.HashSet;
import java.util.Set;

/**
 * A version type provider for Bukkit Minecraft versions.
 * Since these do not show up in the CurseForge API, we have to hardcode the version type.
 */
public class BukkitMinecraftVersionTypeProvider implements VersionTypeProvider {
    @Override
    public Set<Long> getValidVersionTypes(VersionType[] versionTypes) {
        final Set<Long> validVersionTypes = new HashSet<>();
        validVersionTypes.add(1L);
        return validVersionTypes;
    }
}
