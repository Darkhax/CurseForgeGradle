package net.darkhax.curseforgegradle;

import net.darkhax.curseforgegradle.api.versions.GameVersions;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.annotation.Nullable;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class TaskPublishCurseForge extends DefaultTask {

    /**
     * An internal logger instance used to print warnings, errors, and debug information. The logger name includes the
     * name of the project that defined this task and the name of the task.
     */
    private final Logger log;

    /**
     * An internal object that fetches and holds the valid game versions for the current game. This will be null until
     * the {@link #initialize()} step has occurred.
     */
    @Nullable
    private GameVersions validGameVersions;

    /**
     * An internal list of all top-level artifacts that this task should publish. New artifacts are added to this list
     * by using {@link #upload(Object, Object)} during the task configuration phase. These artifacts will be published
     * to CurseForge during the {@link #publish()} step.
     */
    private final List<UploadArtifact> uploadArtifacts = new LinkedList<>();

    /**
     * The game specific API endpoint. This is used to retrieve lists of valid versions for a game and to help files get
     * uploaded to the right game.
     */
    public String apiEndpoint = "https://minecraft.curseforge.com";

    /**
     * The API token used to publish files on your behalf. This token must have the correct project permissions for the
     * files to be published. These tokens can be generated here: https://authors.curseforge.com/account/api-tokens
     */
    public String apiToken;

    public TaskPublishCurseForge() {

        this.log = Logging.getLogger("CurseForgeGradle/" + this.getProject().getDisplayName() + "/" + this.getName());

        // Ensure publishing takes place after the build task has completed. This is required
        // in some environments such as those with parallel task execution enabled.
        this.mustRunAfter(this.getProject().getTasks().getByName("build"));
    }

    /**
     * Creates a new main level artifact that the plugin will attempt to publish during the {@link #publish()} step.
     * This method requires the minimum amount of information to define an artifact. Further configuration including
     * defining additional sub files can be done by modifying the returned artifact instance.
     *
     * @param projectId The CurseForge project ID to publish this artifact to.
     * @param toUpload  The artifact to upload when this artifact is published. This can accept files, archive tasks,
     *                  and several other types of files. The resolution of this is handled by {@link
     *                  #resolveFile(Object)}.
     * @return An object that represents the artifact being published. This can be used to perform additional
     * configuration such as defining a changelog.
     */
    public UploadArtifact upload(Object projectId, Object toUpload) {

        final UploadArtifact artifact = new UploadArtifact(toUpload, parseLong(projectId), this.log, null);
        this.uploadArtifacts.add(artifact);
        return artifact;
    }

    /**
     * This method is called when a gradle defined implementation of this task has been invoked. The project and the
     * task should already be configured at this point.
     */
    @TaskAction
    public void apply() {

        if (!this.uploadArtifacts.isEmpty()) {

            // The execution of this task is split into two steps.

            // The initialize step is used to validate the task configuration and request additional data from the API
            // that is required to process the configuration data into a format the API can understand.
            this.initialize();

            // The publishing step will iterate through all upload artifacts and publish them to CurseForge one by one.
            // The child files of an artifact will be uploaded after the parent artifact has been uploaded and the
            // upload response has been validated.
            this.publish();
        }

        else {

            this.log.warn("No upload artifacts were specified.");
        }
    }

    /**
     * Validates the task configuration and sets up data required for publishing artifacts.
     */
    private void initialize() {

        this.log.debug("Initializing upload task.");

        // An API token is required to publish a file.
        if (apiToken == null) {

            this.log.error("No API token was provided. The file could not be published!");
            throw new GradleException("Can not publish to CurseForge. No API token provided!");
        }

        this.log.debug("Task configured to connect to {}", this.apiEndpoint);

        // Request game version data from the API. This is used to map version slugs to API version IDs.
        this.validGameVersions = new GameVersions(this.apiEndpoint, this.getProject().getDisplayName(), this.getName());
        this.validGameVersions.refresh(this.apiToken);
    }

    /**
     * Attempts to publish all configured artifacts through the API.
     */
    private void publish() {

        // Each artifact goes through two steps. The prepare step is used to process the artifact configuration into
        // a format accepted by the API. The second step is the upload step which posts an upload request to the API
        // and processes the response.
        for (UploadArtifact artifact : this.uploadArtifacts) {

            artifact.prepareForUpload(this.validGameVersions, this::resolveFile);
            artifact.beginUpload(this.apiEndpoint, this.apiToken);

            // Handle additional files, sometimes called sub files or child files.
            for (UploadArtifact childArtifact : artifact.getAdditionalArtifacts()) {

                childArtifact.prepareForUpload(this.validGameVersions, this::resolveFile);
                childArtifact.beginUpload(this.apiEndpoint, this.apiToken);
            }
        }
    }

    /**
     * Resolves a file from an object. This supports several ways of defining files such as NIO files, archive tasks, or
     * standard Gradle project file resolution.
     *
     * @param input The input object to resolve.
     * @return The resolved File reference.
     */
    private File resolveFile(Object input) {

        // If the input is a Java file simply cast and return.
        if (input instanceof File) {

            return (File) input;
        }

        // If the input is an archive task, request the archive file. This allows stuff like the jar task to be passed
        // in directly.
        else if (input instanceof AbstractArchiveTask) {

            return ((AbstractArchiveTask) input).getArchiveFile().get().getAsFile();
        }

        // Fallback to Gradle's project based file lookup.
        return this.getProject().file(input);
    }

    /**
     * Parses a long value from an object. This currently supports numbers and strings.
     *
     * @param obj The value to resolve.
     * @return The resolved Long value.
     */
    public static Long parseLong(Object obj) {

        if (obj instanceof Number) {

            return ((Number) obj).longValue();
        }

        else if (obj instanceof String) {

            return Long.parseLong((String) obj);
        }

        throw new GradleException("Could not parse long from " + obj.getClass().getName() + " of value " + obj);
    }

    /**
     * Gradle can be annoying and represent strings as non-string objects. This allows a variety of data types to be
     * accepted.
     *
     * @param obj The value to resolve.
     * @return The resolved value.
     */
    public static String parseString(Object obj) {

        return obj.toString();
    }
}