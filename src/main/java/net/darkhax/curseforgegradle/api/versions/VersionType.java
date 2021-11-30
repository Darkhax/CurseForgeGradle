package net.darkhax.curseforgegradle.api.versions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a type of game version. In the Minecraft space these can look like Java versions, Mod Loaders, and
 * Minecraft Java Edition versions.
 */
public class VersionType {

    /**
     * The ID of the version type.
     */
    @Expose
    @SerializedName("id")
    private Long id;

    /**
     * The name of the version.
     */
    @Expose
    @SerializedName("name")
    private String name;

    /**
     * The slug of the version.
     */
    @Expose
    @SerializedName("slug")
    private String slug;

    /**
     * Gets the version type ID.
     *
     * @return The version type ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the version type name.
     *
     * @return The version type name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the version type slug.
     *
     * @return The version type slug.
     */
    public String getSlug() {
        return slug;
    }
}