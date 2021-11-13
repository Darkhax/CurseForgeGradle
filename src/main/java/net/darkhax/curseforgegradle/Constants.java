package net.darkhax.curseforgegradle;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Set;

public class Constants {

    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final String EMBEDDED = "embeddedLibrary";
    public static final String INCOMPATIBLE = "incompatible";
    public static final String OPTIONAL = "optionalDependency";
    public static final String REQUIRED = "requiredDependency";
    public static final String TOOL = "tool";
    public static final Set<String> VALID_RELATION_TYPES = ImmutableSet.of(EMBEDDED, INCOMPATIBLE, OPTIONAL, REQUIRED, TOOL);

    public static final String CHANGELOG_TEXT = "text";
    public static final String CHANGELOG_HTML = "html";
    public static final String CHANGELOG_MARKDOWN = "markdown";
    public static final Set<String> VALID_CHANGELOG_TYPES = ImmutableSet.of(CHANGELOG_TEXT, CHANGELOG_HTML, CHANGELOG_MARKDOWN);

    public static final String RELEASE_TYPE_ALPHA = "alpha";
    public static final String RELEASE_TYPE_BETA = "beta";
    public static final String RELEASE_TYPE_RELEASE = "release";
    public static final Set<String> VALID_RELEASE_TYPES = ImmutableSet.of(RELEASE_TYPE_ALPHA, RELEASE_TYPE_BETA, RELEASE_TYPE_RELEASE);
}