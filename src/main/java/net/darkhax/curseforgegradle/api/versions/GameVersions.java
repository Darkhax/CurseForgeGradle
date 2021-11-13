package net.darkhax.curseforgegradle.api.versions;

import net.darkhax.curseforgegradle.Constants;
import net.darkhax.curseforgegradle.CurseForgeGradlePlugin;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Instances of this class are used to query the CurseForge API for valid game versions. Each instance represents a
 * specific game API and is specific to the project and task that instantiated it.
 */
public final class GameVersions {

    private final Logger log;
    private final String versionsEndpoint;
    private final String versionTypesEndpoint;

    private final Set<Long> validVersionTypes = new HashSet<>();
    private final Map<String, Version> versionsByName = new HashMap<>();
    private final Map<String, Version> versionsBySlug = new HashMap<>();

    public GameVersions(String endpoint, String projectName, String taskName) {

        this.versionsEndpoint = endpoint + "/api/game/versions";
        this.versionTypesEndpoint = endpoint + "/api/game/version-types";
        this.log = Logging.getLogger("CurseForgeGradle/Versions/" + projectName + "/" + taskName);
    }

    /**
     * Discards the current version data and refreshes it with new data from the API.
     *
     * @param apiToken The CurseForge API token required to retrieve game version data.
     */
    public void refresh(String apiToken) {

        this.fetchValidVersionTypes(apiToken);
        this.fetchVersions(apiToken);
    }

    /**
     * Discards the current version type data and refreshes it with new data from the API. The valid version are held by
     * {@link #validVersionTypes}.
     *
     * @param apiToken The CurseForge API token required to retrieve game version data.
     */
    private void fetchValidVersionTypes(String apiToken) {

        this.validVersionTypes.clear();

        log.debug("Fetching game version types from {}.", versionTypesEndpoint);
        try (Reader versionReader = CurseForgeGradlePlugin.fetch(versionTypesEndpoint, apiToken)) {

            final VersionType[] versionTypes = Constants.GSON.fromJson(versionReader, VersionType[].class);

            for (final VersionType type : versionTypes) {

                // TODO this is hardcoded to Minecraft. A more generic solution is required.
                if (type.getSlug().startsWith("minecraft") || type.getSlug().equals("java") || type.getSlug().equals("modloader")) {

                    this.validVersionTypes.add(type.getId());
                }

                log.debug("Received game version type {} with id {}.", type.getName(), type.getId());
            }
        }

        catch (IOException e) {

            log.error("Failed to fetch game version types!", e);
            throw new GradleException("Failed to fetch game versions!", e);
        }
    }

    /**
     * Discards the current game versions data and refreshes it with new data from the API. Only game versions with a
     * valid type as determined by {@link #validVersionTypes} will be included. The fetched data will be held in the
     * {@link #versionsByName} and {@link #versionsBySlug} maps.
     *
     * @param apiToken The CurseForge API token required to retrieve game version data.
     */
    private void fetchVersions(String apiToken) {

        this.versionsByName.clear();
        this.versionsBySlug.clear();

        log.debug("Fetching game versions from {}.", versionsEndpoint);
        try (Reader versionReader = CurseForgeGradlePlugin.fetch(versionsEndpoint, apiToken)) {

            final Version[] versions = Constants.GSON.fromJson(versionReader, Version[].class);

            for (final Version version : versions) {

                if (this.validVersionTypes.contains(version.getGameVersionTypeID())) {

                    versionsByName.compute(version.getName(), (name, existing) -> {

                        if (existing != null) {
                            log.warn("Version name {} was already present. Former ID {}. New ID {}.", version.getName(), existing.getId(), version.getId());
                        }

                        return version;
                    });

                    versionsBySlug.compute(version.getSlug(), (slug, existing) -> {

                        if (existing != null) {
                            log.warn("Version slug {} was already present. Former ID {}. New ID {}.", version.getSlug(), existing.getId(), version.getId());
                        }

                        return version;
                    });
                }

                log.debug("Received game version {} with id {}.", version.getName(), version.getId());
            }
        }

        catch (IOException e) {

            log.error("Failed to fetch game versions!", e);
            throw new GradleException("Failed to fetch game versions!", e);
        }
    }

    /**
     * Gets a Version by it's name or slug. The version name takes priority over the version slug when matching. Matches
     * made by this method are case sensitive!
     *
     * @param versionString The version to lookup.
     * @return The game version that was found. A null value indicates that the version is likely invalid.
     */
    public Version getVersion(String versionString) {

        Version foundVersion = null;

        // Check the version by name cache first.
        foundVersion = versionsByName.get(versionString);

        // If no version name was found, try as a slug.
        if (foundVersion == null) {

            foundVersion = versionsBySlug.get(versionString);
        }

        return foundVersion;
    }

    /**
     * Resolves a set of version names/slugs into their CurseForge API Ids. If a given version candidate is not valid an
     * exception will be raised.
     *
     * @param toResolve The set of version names and slugs to resolve.
     * @return A set of CurseForge API Ids for the valid version candidates.
     */
    public Set<Long> resolveVersions(Set<String> toResolve) {

        final Set<Long> validVersions = new HashSet<>();

        for (String versionCandidate : toResolve) {

            final Version resolved = this.getVersion(versionCandidate);

            if (resolved == null) {

                log.error("Version {} is not valid for this game!", versionCandidate);
                throw new GradleException("Version " + versionCandidate + " is not valid for this game!");
            }

            else {

                validVersions.add(resolved.getId());
            }
        }

        return validVersions;
    }
}