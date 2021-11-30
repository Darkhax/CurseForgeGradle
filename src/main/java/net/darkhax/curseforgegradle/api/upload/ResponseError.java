package net.darkhax.curseforgegradle.api.upload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a custom error message from the CurseForge API.
 */
public final class ResponseError {

    /**
     * The error code from CurseForge.
     */
    @Expose
    @SerializedName("errorCode")
    private int code;

    /**
     * The error message from CurseForge.
     */
    @Expose
    @SerializedName("errorMessage")
    private String message;

    /**
     * Gets the error code.
     *
     * @return The error code.
     */
    public int getCode() {

        return this.code;
    }

    /**
     * Gets the error message.
     *
     * @return The error message.
     */
    public String getMessage() {

        return this.message;
    }
}