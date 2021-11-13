package net.darkhax.curseforgegradle.api.versions;

import com.google.gson.annotations.Expose;

/**
 * Represents a valid game version result from a CurseForge API.
 */
public class Version {

    @Expose
    private long id;

    @Expose
    private long gameVersionTypeID;

    @Expose
    private String slug;

    @Expose
    private String name;

    public long getId() {
        return this.id;
    }

    public long getGameVersionTypeID() {

        return this.gameVersionTypeID;
    }

    public String getSlug() {

        return this.slug;
    }

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