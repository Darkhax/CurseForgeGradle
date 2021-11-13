package net.darkhax.curseforgegradle.api.versions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a type of game version. In the Minecraft space these can look like Java versions, Mod Loaders, and
 * Minecraft Java Edition versions.
 */
public class VersionType {

    @Expose
    @SerializedName("id")
    private Long id;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("slug")
    private String slug;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
}