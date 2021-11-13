package net.darkhax.curseforgegradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class CurseForgeGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getLogger().debug("Applying CurseForgeGradle plugin to project {}", project.getDisplayName());
    }

    /**
     * Creates an HTTP reader with an optional API token that is specified in a format accepted by the CurseForge API.
     *
     * @param urlString The URL as a string.
     * @param token     An optional API token.
     * @return The HTTP reader.
     * @throws IOException This exception will be raised if the connection was rejected or could not be established.
     */
    public static Reader fetch(String urlString, @Nullable String token) throws IOException {

        final URL url = new URL(urlString);

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "CurseForgeGradle");
        connection.setRequestProperty("Accept-Encoding", "gzip");

        if (token != null) {

            connection.addRequestProperty("X-Api-Token", token);
        }

        return getHttpReader(connection);
    }

    /**
     * Creates an HTTP reader that can accept GZip encoded streams when possible.
     *
     * @param connection The connection to read.
     * @return The HTTP reader.
     */
    private static Reader getHttpReader(HttpURLConnection connection) throws IOException {

        // If the server accepts GZip, use the GZip stream for faster communication.
        if ("gzip".equals(connection.getContentEncoding())) {

            return new InputStreamReader(new GZIPInputStream(connection.getInputStream()));
        }

        // The fallback is just a normal input stream.
        else {

            return new InputStreamReader(connection.getInputStream());
        }
    }
}