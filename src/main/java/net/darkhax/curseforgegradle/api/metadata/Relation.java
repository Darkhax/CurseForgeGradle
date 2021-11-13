package net.darkhax.curseforgegradle.api.metadata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a project relationship in the CurseForge upload metadata.
 */
public class Relation {

    @Expose
    @SerializedName("slug")
    private String slug;

    @Expose
    @SerializedName("type")
    private String type;

    public Relation(String slug, String type) {

        this.slug = slug;
        this.type = type;
    }

    public String getSlug() {

        return this.slug;
    }

    public String getType() {

        return this.type;
    }
}
