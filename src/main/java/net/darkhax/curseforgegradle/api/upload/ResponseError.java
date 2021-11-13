package net.darkhax.curseforgegradle.api.upload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a custom error message from the CurseForge API.
 */
public class ResponseError {

    @Expose
    @SerializedName("errorCode")
    private int code;

    @Expose
    @SerializedName("errorMessage")
    private String message;

    public int getCode() {

        return this.code;
    }

    public String getMessage() {

        return this.message;
    }
}