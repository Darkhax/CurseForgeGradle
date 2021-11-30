package net.darkhax.curseforgegradle.api.upload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This POJO represents a response from the CurseForge API when a file has been successfully uploaded.
 */
public final class ResponseSuccessful {

    /**
     * The ID of the file that was uploaded. This will not be publicly usable until the file has been processed and
     * approved by CurseForge however it can still be used for uploading additional artifacts or debugging.
     */
    @Expose
    @SerializedName("id")
    private long id;

    /**
     * Gets the file ID of the newly uploaded file.
     *
     * @return The returned file ID.
     */
    public long getId() {

        return this.id;
    }
}
