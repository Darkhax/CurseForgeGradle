package net.darkhax.curseforgegradle.api.metadata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

/**
 * An object that serves as a container for relationships to other projects.
 */
public final class ProjectRelations {

    /**
     * Contains all relationships with other projects.
     */
    @Expose
    @SerializedName("projects")
    private final Set<Relation> projectRelations = new HashSet<>();

    /**
     * Adds a new project relationship.
     *
     * @param slug The slug of the other project.
     * @param type The type of relation to add. See {@link net.darkhax.curseforgegradle.Constants#VALID_RELATION_TYPES}
     *             for types that are known to work.
     */
    public void addRelationship(String slug, String type) {

        this.projectRelations.add(new Relation(slug, type));
    }

    /**
     * Gets the set of project relationships.
     *
     * @return The set of project relationships.
     */
    public Set<Relation> getRelations() {

        return this.projectRelations;
    }
}