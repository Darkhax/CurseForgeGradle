package net.darkhax.curseforgegradle.api.metadata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a project relationship in the CurseForge upload metadata.
 */
public class Relation {

    /**
     * The slug of the project.
     */
    @Expose
    @SerializedName("slug")
    public String slug;

    /**
     * The type of relationship.
     */
    @Expose
    @SerializedName("type")
    public String type;

    /**
     * Creates a new project relationship.
     *
     * @param slug The slug of the project.
     * @param type The type of relationship.
     */
    protected Relation(String slug, String type) {

        this.slug = slug;
        this.type = type;
    }
}
