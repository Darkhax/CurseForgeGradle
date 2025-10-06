package net.darkhax.curseforgegradle.api.metadata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

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
     * The type of relationship.
     */
    @Expose
    @SerializedName("id")
    @Nullable
    public String id;

    /**
     * Creates a new project relationship.
     *
     * @param slug The slug of the project.
     * @param type The type of relationship.
     */
    protected Relation(String slug, String type, @Nullable String id) {

        this.slug = slug;
        this.type = type;
        this.id = id;
    }
}
