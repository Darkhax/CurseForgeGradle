package net.darkhax.curseforgegradle.api.metadata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

/**
 * An object that serves as a container for relationships to other projects.
 */
public class ProjectRelations {

    @Expose
    @SerializedName("projects")
    private final Set<Relation> projectRelations = new HashSet<>();

    public void addRelationship(String slug, String type) {

        this.projectRelations.add(new Relation(slug, type));
    }

    public Set<Relation> getRelations() {

        return this.projectRelations;
    }
}