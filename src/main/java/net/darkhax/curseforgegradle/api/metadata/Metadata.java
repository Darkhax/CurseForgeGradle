package net.darkhax.curseforgegradle.api.metadata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.darkhax.curseforgegradle.Constants;

import java.util.HashSet;
import java.util.Set;

/**
 * A POJO that represents the metadata value in a CurseForge upload request. This object exists to be serialized to the
 * JSON used to make that request.
 */
public final class Metadata {

    /**
     * The changelog string. OPTIONAL
     */
    @Expose
    @SerializedName("changelog")
    public String changelog = null;

    /**
     * The changelog type. OPTIONAL
     */
    @Expose
    @SerializedName("changelogType")
    public String changelogType = Constants.CHANGELOG_TEXT;

    /**
     * The display name. OPTIONAL
     */
    @Expose
    @SerializedName("displayName")
    public String displayName = null;

    /**
     * The parent file ID. OPTIONAL
     */
    @Expose
    @SerializedName("parentFileID")
    public Long parentFileID = null;

    /**
     * The supported game versions. REQUIRED SOMETIMES
     * <p>
     * When {@link #parentFileID} is null this must be a non-empty set. If {@link #parentFileID} is not null this must
     * be null.
     */
    @Expose
    @SerializedName("gameVersions")
    public Set<Long> gameVersions = new HashSet<>();

    /**
     * The release type. REQUIRED
     */
    @Expose
    @SerializedName("releaseType")
    public String releaseType = Constants.RELEASE_TYPE_ALPHA;

    /**
     * The relationship metadata. OPTIONAL
     * <p>
     * If {@link ProjectRelations#getRelations()} is empty this must be null.
     */
    @Expose
    @SerializedName("relations")
    public ProjectRelations relations = null;
}