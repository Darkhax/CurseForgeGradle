package net.darkhax.curseforgegradle.api.versions;

import com.google.gson.annotations.Expose;

/**
 * Represents a valid game version result from a CurseForge API.
 */
public class Version {

    /**
     * The internal version ID.
     */
    @Expose
    private long id;

    /**
     * The type of version ID.
     */
    @Expose
    private long gameVersionTypeID;

    /**
     * The slug for this version.
     */
    @Expose
    private String slug;

    /**
     * The name of the version.
     */
    @Expose
    private String name;

    /**
     * Gets the internal CurseForge version ID.
     *
     * @return The internal CurseForge version ID.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Gets the version type ID.
     *
     * @return The version type ID.
     */
    public long getGameVersionTypeID() {

        return this.gameVersionTypeID;
    }

    /**
     * Gets the version slug.
     *
     * @return The version slug.
     */
    public String getSlug() {

        return this.slug;
    }

    /**
     * Gets the version name.
     *
     * @return The version name.
     */
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return getId() == ((Version) obj).getId();
    }

    @Override
    public int hashCode() {

        return Long.hashCode(this.getId());
    }
}