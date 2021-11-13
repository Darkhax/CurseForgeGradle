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
public class Metadata {

    @Expose
    @SerializedName("changelog") // Optional
    public String changelog = null;

    @Expose
    @SerializedName("changelogType") // Optional
    public String changelogType = Constants.CHANGELOG_TEXT;

    @Expose
    @SerializedName("displayName") // Optional
    public String displayName = null;

    @Expose
    @SerializedName("parentFileID") // Optional
    public Long parentFileID = null;

    @Expose
    @SerializedName("gameVersions")
    public Set<Long> gameVersions = new HashSet<>();

    @Expose
    @SerializedName("releaseType")
    public String releaseType = Constants.RELEASE_TYPE_ALPHA;

    @Expose
    @SerializedName("relations") // Optional
    public ProjectRelations relations = new ProjectRelations();
}