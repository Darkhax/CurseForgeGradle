package net.darkhax.curseforgegradle;

import com.google.common.collect.ImmutableList;
import java.util.StringJoiner;
import net.darkhax.curseforgegradle.api.metadata.Metadata;
import net.darkhax.curseforgegradle.api.metadata.ProjectRelations;
import net.darkhax.curseforgegradle.api.upload.ResponseError;
import net.darkhax.curseforgegradle.api.upload.ResponseSuccessful;
import net.darkhax.curseforgegradle.api.versions.GameVersions;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class defines the script-time representation of an artifact being published to CurseForge. Users will directly
 * configure this object through their script when defining a new file to upload.
 */
public class UploadArtifact {

    // --- INTERNAL PROPERTIES --- //

    private final ObjectFactory objectFactory;

    /**
     * An internal logger used to log information about the upload process. This logger includes the name of the project
     * and the task that is publishing the artifact.
     */
    private final Logger log;

    /**
     * The CurseForge project ID to upload this artifact and all child artifacts to. All child artifacts must have the
     * same project ID as their parent artifact. This is intentionally immutable to dissuade poorly configured
     * projects.
     */
    private final Long projectId;

    /**
     * An internal reference to the parent artifact. This is only used when the current artifact is a child artifact and
     * will be null otherwise. An artifact can only have one parent but a parent artifact can have many children.
     */
    private final UploadArtifact parent;

    /**
     * An internal reference to the artifact being uploaded. This reference is held as an object to account for the
     * various ways files can be represented in a Gradle project. This will be resolved to a NIO File reference during
     * the {@link #prepareForUpload(GameVersions)} step. The result of which is held by {@link #uploadFile}.
     */
    private final FileCollection artifact;

    /**
     * An internal reference to the upload artifact as a NIO File. This is null until the {@link
     * #prepareForUpload(GameVersions)} step has happened.
     */
    @Nullable
    private File uploadFile = null;

    /**
     * The ID of the file on CurseForge. This is supplied by CurseForge after the file has been successfully uploaded
     * and will remain null until the file has been uploaded.
     */
    @Nullable
    @Internal
    private Long curseFileId;

    /**
     * An internal set of the CurseForge game version tags applicable for this file. These IDs are not guaranteed to be
     * consistent across uploads, so they must be resolved using a separate API call. This set is resolved using values
     * from {@link #gameVersions} during {@link #prepareForUpload(GameVersions)}.
     */
    @Nullable
    private Set<Long> uploadVersions;

    /**
     * An internal list of additional files that will be uploaded as children to this artifact when this artifact is
     * uploaded. TODO explain where this happens
     */
    private final List<UploadArtifact> additionalFiles = new ArrayList<>();

    /**
     * An internal map of the relationships defined for this file and other files. These relationships are used by users
     * and launchers to identify things like required dependencies or incompatible mods.
     * <p>
     * When a sub file is created using {@link #withAdditionalFile(Object)} it will inherit the current changelog value.
     * This can be changed independently after creation.
     */
    private Map<String, String> relationships = new HashMap<>();

    /**
     * An internal object that holds all project relationships for the artifact. This will be created from the values of
     * {@link #relationships} during the {@link #prepareForUpload(GameVersions)} step.
     */
    private final ProjectRelations uploadRelations = new ProjectRelations();

    // --- TASK PROPERTIES --- //

    /**
     * An optional changelog for this file. This is displayed on the CurseForge website, and it's use is highly
     * recommended. For best results this should be defined using a UTF-8 string.
     * <p>
     * When a sub file is created using {@link #withAdditionalFile(Object)} it will inherit the current changelog value.
     * This can still be changed independently after creation.
     */
    public Object changelog = null;

    /**
     * The type of changelog being defined. CurseForge supports various formats such as markdown and HTML however the
     * default format is plaintext.
     * <p>
     * When a sub file is created using {@link #withAdditionalFile(Object)} it will inherit the current changelog type.
     * This can still be changed independently after creation.
     */
    public Object changelogType = Constants.CHANGELOG_TEXT;

    /**
     * The display name for the file on CurseForge. When defined this will hide the name of the file on CurseForge. The
     * use of this property is generally discouraged.
     */
    public Object displayName = null;

    /**
     * A set of game versions associated with the artifact. At least one game version is required to upload an artifact.
     * Additional meta tags like Java version or Loader version are also considered game versions by CurseForge and are
     * added here.
     * <p>
     * Sub files automatically inherit the game versions of their parent file. This is a hard limit enforced by the
     * CurseForge API and can not be changed after the fact.
     */
    public Set<String> gameVersions = new HashSet<>();

    /**
     * The type of release for this file. The default release type is an alpha. When using something like CI to automate
     * bleeding edge releases it is recommended to retain the alpha release type.
     * <p>
     * When a sub file is created using {@link #withAdditionalFile(Object)} it will inherit the current release type.
     * This can still be changed independently after creation.
     */
    public Object releaseType = Constants.RELEASE_TYPE_ALPHA;

    /**
     * These are created using a helper method from TaskPublishCurseForge. Users should never construct this manually.
     *
     * @param artifact      The artifact to publish. This is not necessarily a file and may not be valid until later in the
     *                      build process.
     * @param projectId     The ID of the project to publish this artifact to.
     * @param objectFactory The project-local object factory.
     * @param log           A logger used to help with debugging. This is taken from the Task that define the artifact and
     *                      is unique to each task.
     * @param parent        An optional parent artifact. When defined the current artifact is treated as a
     *                      child/additional/sub file.
     */
    protected UploadArtifact(Object artifact, Long projectId, ObjectFactory objectFactory, Logger log, @Nullable UploadArtifact parent) {
        this.objectFactory = objectFactory;
        this.log = log;
        this.projectId = projectId;
        this.parent = parent;

        ConfigurableFileCollection artifactContainer = objectFactory.fileCollection().from(artifact);
        artifactContainer.disallowChanges();
        this.artifact = artifactContainer;
    }

    @InputFiles
    public FileCollection getArtifact() {
        return artifact;
    }

    /**
     * Creates a new additional file that will be uploaded along with this main file. These files are sometimes called
     * child files or sub files. Only parent files can have additional files. Attempting to create an additional file on
     * another additional file is unsupported.
     * <p>
     * By default additional files will copy the changelog, changelog type, release type, and project relations from the
     * parent. Properties copied to an additional file can be modified after the fact however some properties like game
     * versions are exclusively defined by the parent file.
     *
     * @param file The file to publish.
     * @return An object that represents the artifact being published. This can be used to perform additional
     * configuration such as defining a changelog.
     */
    public UploadArtifact withAdditionalFile(Object file) {

        if (this.parent != null) {

            this.log.error("Artifact {} is a child of artifact {}. Artifacts must only be nested one layer deep!", this, this.parent);
            throw new GradleException("Child artifacts must not have their own children. Artifacts can only be nested one layer deep.");
        }

        final UploadArtifact subFile = new UploadArtifact(file, this.projectId, this.objectFactory, this.log, this);
        subFile.changelogType = this.changelogType;
        subFile.changelog = this.changelog;
        subFile.releaseType = this.releaseType;
        subFile.relationships = new HashMap<>(this.relationships);

        this.additionalFiles.add(subFile);
        return subFile;
    }

    /**
     * Marks another project as being incompatible with this file. This will warn users not to use that project with
     * yours. It may also prevent that project from being installed with a launcher when this file is already
     * installed.
     *
     * @param slugs The slugs of the incompatible projects.
     */
    public void addIncompatibility(Object... slugs) {

        this.addRelations(Constants.RELATION_INCOMPATIBLE, slugs);
    }

    /**
     * Marks another project as being required for this file to work properly. This will advise users to download the
     * required project when they download this file. It may also cause the latest compatible version of that project to
     * be installed automatically when this file is installed through a launcher.
     *
     * @param slugs The slug of the required projects.
     */
    public void addRequirement(Object... slugs) {

        this.addRelations(Constants.RELATION_REQUIRED, slugs);
    }

    /**
     * Marks another project as being embedded within this file.
     *
     * @param slugs The slugs of the embedded projects.
     */
    public void addEmbedded(Object... slugs) {

        this.addRelations(Constants.RELATION_EMBEDDED, slugs);
    }

    /**
     * Marks another project as being a tool for this file. Nobody seems to know what this means.
     *
     * @param slugs The slugs of the tool projects.
     */
    public void addTool(Object... slugs) {

        this.addRelations(Constants.RELATION_TOOL, slugs);
    }

    /**
     * Marks another project as being optional. This is used to let users know that this file has special support for
     * another project or works really well with that project.
     *
     * @param slugs The slugs of the optional projects.
     */
    public void addOptional(Object... slugs) {

        this.addRelations(Constants.RELATION_OPTIONAL, slugs);
    }

    /**
     * Marks the file as supporting a given modloader. This is primarily used by Minecraft for the Forge, Fabric, and
     * Rift loaders.
     *
     * @param modloaders The modloaders that are supported by this file.
     */
    public void addModLoader(Object... modloaders) {

        // Mod loaders are considered game versions for now.
        addGameVersion(modloaders);
    }

    /**
     * Marks the file as supporting the given Java versions.
     *
     * @param javaVersions The java versions that are supported.
     */
    public void addJavaVersion(Object... javaVersions) {

        // Java versions are considered game versions for now.
        addGameVersion(javaVersions);
    }

    /**
     * Marks the file as supporting a given game versions.
     *
     * @param gameVersions The game versions supported by this file.
     */
    public void addGameVersion(Object... gameVersions) {

        if (this.parent != null) {

            StringJoiner versionString = new StringJoiner(", ");
            for (Object gameVersion : gameVersions) {

                versionString.add(TaskPublishCurseForge.parseString(gameVersion));
            }

            this.log.error("Attempted to set the version of an additional file. This is not allowed! versions={}", versionString);
            throw new GradleException("Sub files can not have their own versions!");
        }

        for (Object gameVersion : gameVersions) {

            final String versionString = TaskPublishCurseForge.parseString(gameVersion);
            this.gameVersions.add(versionString);
        }
    }

    /**
     * Adds a relationship between this artifact and multiple other projects on CurseForge. This can have different connotations
     * depending on the game and the platform consuming this data. For example in the case of a Minecraft mod defining a
     * required dependency relationship will cause the official CurseForge launcher to automatically download a valid
     * version of that project when this file is requested.
     *
     * @param type  The type of relationship to define.
     * @param slugs The slug of the project to define a relationship with.
     */
    public void addRelations(Object type, Object... slugs) {

        for (Object slug : slugs) {

            addRelation(slug, type);
        }
    }

    /**
     * Adds a relationship between this artifact and another project on CurseForge. This can have different connotations
     * depending on the game and the platform consuming this data. For example in the case of a Minecraft mod defining a
     * required dependency relationship will cause the official CurseForge launcher to automatically download a valid
     * version of that project when this file is requested.
     *
     * @param slug The slug of the project to define a relationship with.
     * @param type The type of relationship to define.
     */
    public void addRelation(Object slug, Object type) {

        final String slugString = TaskPublishCurseForge.parseString(slug);
        final String existingRelation = relationships.get(slugString);
        final String typeString = TaskPublishCurseForge.parseString(type);

        if (!Constants.VALID_RELATION_TYPES.contains(typeString)) {

            this.log.warn("Unknown relation type {} was defined for project {}.", typeString, slugString);
        }

        if (existingRelation != null) {

            if (typeString == null) {

                this.relationships.remove(slugString);
                this.log.warn("Relation with project {} has been removed.", slugString);
            }

            else {

                this.log.warn("Changing relation type for project {} from {} to {}.", slugString, existingRelation, typeString);
            }
        }

        if (typeString != null) {

            this.relationships.put(slugString, typeString);
        }
    }

    /**
     * Prepares the artifact for being uploaded. This will resolve some configured properties into a format consumable
     * by the API. This is intended for internal use.
     *
     * @param validGameVersions The valid game version data from the API.
     */
    public final void prepareForUpload(GameVersions validGameVersions) {

        this.uploadFile = this.artifact.getSingleFile();

        // Make sure the file being uploaded actually exists.
        if (!this.uploadFile.exists()) {

            this.log.error("Could not find the file to upload. Expected {}", uploadFile.getAbsolutePath());
            throw new GradleException("The expected upload artifact does not exist!", new FileNotFoundException(uploadFile.getAbsolutePath()));
        }

        this.log.debug("Preparing to upload file {}.", this.uploadFile.getName());

        String parsedChangelogType = TaskPublishCurseForge.parseString(this.changelogType);
        String parsedReleaseType = TaskPublishCurseForge.parseString(this.releaseType);

        // Make sure a valid changelog type is being used.
        if (!Constants.VALID_CHANGELOG_TYPES.contains(parsedChangelogType)) {

            this.log.warn("Changelog type {} is not recognized. This may cause issues!", parsedChangelogType);
        }

        // Make sure a valid release type is being used.
        if (!Constants.VALID_RELEASE_TYPES.contains(parsedReleaseType)) {

            this.log.warn("Release type {} is not recognized. This may cause issues!", parsedReleaseType);
        }

        // Make sure all file relationships are valid. The project slugs are not tested because it's not realistic to do that with the current API limitations.
        for (Map.Entry<String, String> relation : this.relationships.entrySet()) {

            final String projectSlug = relation.getKey();
            final String relationType = relation.getValue();

            this.log.debug("File {} will have a {} relationship to project {}.", this.uploadFile.getName(), relationType, projectSlug);

            if (!Constants.VALID_RELATION_TYPES.contains(relation.getValue())) {

                this.log.warn("The relation type {} to project {} for file {} is not recognized.", relationType, projectSlug, uploadFile.getName());
            }

            this.uploadRelations.addRelationship(projectSlug, relationType);
        }

        // Resolve game versions from strings to IDs using the results from the CurseForge API.
        this.uploadVersions = validGameVersions.resolveVersions(this.gameVersions);
    }

    /**
     * Builds the upload file URI for this project.
     *
     * @param endpoint The endpoint to upload the file to.
     */
    private String getUploadTarget(String endpoint) {

        return endpoint + "/api/projects/" + this.projectId + "/upload-file";
    }

    /**
     * Logs the URI this artifact would be uploaded to as well as the metadata for this artifact.
     *
     * @param endpoint The endpoint to upload the file to.
     */
    public final void logUploadMetadata(String endpoint) {

        this.log.lifecycle("Upload file URI for {}: {}", this.uploadFile.getName(), getUploadTarget(endpoint));
        this.log.lifecycle(Constants.PRETTY_GSON.toJson(this.createMetadata()));

        StringJoiner prettyVersions = new StringJoiner(", ");
        for (String gameVersion : this.gameVersions) {

            prettyVersions.add(gameVersion);
        }
        this.log.lifecycle("Game versions: {}", prettyVersions);
    }

    /**
     * Triggers the post request to the API that will begin the upload of the artifact. This is intended for internal
     * use.
     *
     * @param endpoint The endpoint to upload the file to.
     * @param token    The CurseForge API token used to authenticate the upload.
     */
    public final void beginUpload(String endpoint, String token) {

        final HttpClient webClient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).setUserAgent("CurseForgeGradle").build();

        final MultipartEntityBuilder requestEntity = MultipartEntityBuilder.create();
        requestEntity.addTextBody("metadata", Constants.GSON.toJson(this.createMetadata()), ContentType.APPLICATION_JSON);
        requestEntity.addBinaryBody("file", this.uploadFile);

        final HttpPost request = new HttpPost(getUploadTarget(endpoint));
        request.addHeader("X-Api-Token", token);
        request.setEntity(requestEntity.build());

        try {

            this.log.debug("Initiating upload of {}.", this.uploadFile.getName());
            final HttpResponse response = webClient.execute(request);

            // Handles when an upload was successful.
            if (response.getStatusLine().getStatusCode() == 200) {

                final InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
                this.curseFileId = Constants.GSON.fromJson(reader, ResponseSuccessful.class).getId();
                reader.close();
                this.log.debug("Artifact {} uploaded with ID {}.", this.uploadFile.getName(), this.curseFileId);
            }

            // Handles when the upload was rejected by CurseForge.
            else {

                int errorCode = response.getStatusLine().getStatusCode();
                String message = response.getStatusLine().getReasonPhrase();

                // Sometimes CurseForge will give a custom error message so this is handled here.
                if (response.getFirstHeader("content-type").getValue().contains("json")) {

                    final InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
                    ResponseError error = Constants.GSON.fromJson(reader, ResponseError.class);
                    reader.close();

                    errorCode = error.getCode();
                    message = error.getMessage();
                }

                this.log.error("Curse rejected artifact {} with error code '{}' and message '{}'.", this.uploadFile.getName(), errorCode, message);
                throw new GradleException("Failed to upload artifact " + this.uploadFile.getName() + ". Error code '" + errorCode + "', message '" + message + "'.");
            }
        }

        catch (IOException e) {

            this.log.error("Failed to upload artifact {}!", this.uploadFile.getName());
            throw new GradleException("Failed to upload artifact!", e);
        }
    }

    /**
     * Provides an immutable collection of additional artifacts for this file. This is intended for internal use only.
     *
     * @return An immutable collection of additional artifacts.
     */
    @Nested
    public final Collection<UploadArtifact> getAdditionalArtifacts() {
        return ImmutableList.copyOf(this.additionalFiles);
    }

    /**
     * Creates the upload metadata for the artifact.
     *
     * @return The CurseForge uploaded metadata.
     */
    private Metadata createMetadata() {

        final Metadata request = new Metadata();
        request.changelog = TaskPublishCurseForge.parseString(this.changelog);
        request.changelogType = TaskPublishCurseForge.parseString(this.changelogType);
        request.displayName = TaskPublishCurseForge.parseString(this.displayName);
        request.releaseType = TaskPublishCurseForge.parseString(this.releaseType);

        // Only set the relations if they actually exist. Curse doesn't like empty arrays here :upside_down:
        if (!this.uploadRelations.getRelations().isEmpty()) {

            request.relations = this.uploadRelations;
        }

        // If the parent is null this is a parent artifact.
        if (this.parent == null) {

            // Only parent artifacts can define upload versions. The API gets upset if you give it an empty array or
            // an array that matches the parent. Only a null value is accepted for child files.
            request.gameVersions = this.uploadVersions;
        }

        // If the parent is not null this is an additional file / child artifact.
        if (this.parent != null) {

            // Child files must not define a game version array. This is taken from the parent file and is handled by
            // the API backend.
            request.gameVersions = null;

            // Copies the numeric ID of the parent file to the request. This signals to the API that this is a child
            // file. The curseFileId is null until the parent has been published.
            request.parentFileID = this.parent.curseFileId;
        }

        return request;
    }

    @Nullable
    public Long getCurseFileId() {
        return curseFileId;
    }
}