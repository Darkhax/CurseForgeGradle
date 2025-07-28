package net.darkhax.curseforgegradle;

import org.jetbrains.annotations.Nullable;

public class RelationSpecifics {

    private String type;

    @Nullable
    private String id;

    public RelationSpecifics(String type, @Nullable String id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }
}
