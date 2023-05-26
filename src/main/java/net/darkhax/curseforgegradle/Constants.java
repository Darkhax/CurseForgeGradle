package net.darkhax.curseforgegradle;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Set;

/**
 * This class contains all constants repeatedly used by the plugin. The majority of these properties are used to
 * validate known values for enum-like properties.
 */
public class Constants {

    /**
     * A GSON instance that is used by the plugin to serialize objects to JSON. This is primarily used to generate API
     * request bodies and parse responses from the CurseForge API.
     */
    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * A GSON instance that is used by the plugin to serialize objects to JSON for use in more readable logging.
     */
    public static final Gson PRETTY_GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    /**
     * The value for an embedded relationship. This is used when the files you publish contain an embedded version of
     * the other project.
     */
    public static final String RELATION_EMBEDDED = "embeddedLibrary";

    /**
     * The value for an incompatible relationship. This is used when the other project will not work if installed in the
     * same game instance as your own project.
     */
    public static final String RELATION_INCOMPATIBLE = "incompatible";

    /**
     * The value for an optional relationship. This is used when you have special support specifically for another
     * project but the other project is not required.
     */
    public static final String RELATION_OPTIONAL = "optionalDependency";

    /**
     * The value for a required relationship. This is used when your file requires a file from the other project and
     * will not work without it.
     */
    public static final String RELATION_REQUIRED = "requiredDependency";

    /**
     * The value for a tool relationship. Nobody really knows what this does.
     */
    public static final String RELATION_TOOL = "tool";

    /**
     * An immutable set of all relation types that are known to be valid. This is often used to validate user input.
     */
    public static final Set<String> VALID_RELATION_TYPES = ImmutableSet.of(RELATION_EMBEDDED, RELATION_INCOMPATIBLE, RELATION_OPTIONAL, RELATION_REQUIRED, RELATION_TOOL);

    /**
     * The value for a plain text changelog. No special formatting is applied when this is used.
     */
    public static final String CHANGELOG_TEXT = "text";

    /**
     * The value for an HTML changelog. Only a subset of HTML is supported.
     */
    public static final String CHANGELOG_HTML = "html";

    /**
     * The value for a Markdown changelog. Only a subset of markdown is supported.
     */
    public static final String CHANGELOG_MARKDOWN = "markdown";

    /**
     * An immutable set of all changelog types known to be valid. This is often used to validate user input.
     */
    public static final Set<String> VALID_CHANGELOG_TYPES = ImmutableSet.of(CHANGELOG_TEXT, CHANGELOG_HTML, CHANGELOG_MARKDOWN);

    /**
     * The value for an alpha release. These files are often hidden from certain API responses and user views.
     */
    public static final String RELEASE_TYPE_ALPHA = "alpha";

    /**
     * The value for a beta release. These files are often hidden from certain API responses and user views.
     */
    public static final String RELEASE_TYPE_BETA = "beta";

    /**
     * The value for a full release. These are often considered stable/promoted builds.
     */
    public static final String RELEASE_TYPE_RELEASE = "release";

    /**
     * An immutable set of all release types known to be valid. This is often used to validate user input.
     */
    public static final Set<String> VALID_RELEASE_TYPES = ImmutableSet.of(RELEASE_TYPE_ALPHA, RELEASE_TYPE_BETA, RELEASE_TYPE_RELEASE);
}