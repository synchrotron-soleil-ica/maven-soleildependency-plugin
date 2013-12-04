package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.domain;

import fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.exception.MavenDependencyPluginException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Gregory Boissinot
 */
public class CustomArtifact extends DefaultArtifact {

    private Date creationDate;

    public CustomArtifact(Artifact artifact) {
        super(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersionRange(),
                artifact.getScope(),
                artifact.getType(),
                artifact.getClassifier(),
                artifact.getArtifactHandler());
    }


    public CustomArtifact(String groupId, String artifactId, VersionRange versionRange, String scope, String type, String classifier, ArtifactHandler artifactHandler) {
        super(groupId, artifactId, versionRange, scope, type, classifier, artifactHandler);
    }

    public CustomArtifact(String groupId, String artifactId, VersionRange versionRange, String scope, String type, String classifier, ArtifactHandler artifactHandler, boolean optional) {
        super(groupId, artifactId, versionRange, scope, type, classifier, artifactHandler, optional);
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    private static final char CONSOLE_DELIMITER = ':';
    private static final char CSV_DELIMITER = ';';

    public String toString() {
        return toString(CONSOLE_DELIMITER);
    }

    public static String toCsvHeaders() {
        return toHeaders(CSV_DELIMITER);
    }

    public static String toHeaders(char delimiter) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.append("ORGANISATION;NAME;EXTENSION;VERSION;SCOPE;PUB_DATE");
        return stringWriter.toString();
    }

    public String toString(char delimiter) {
        StringBuffer sb = new StringBuffer();
        if (getGroupId() != null) {
            sb.append(getGroupId());
            sb.append(delimiter);
        }
        appendArtifactTypeClassifierString(delimiter, sb);
        sb.append(delimiter);
        if (getBaseVersionInternal() != null) {
            sb.append(getBaseVersionInternal());
        } else {
            sb.append(super.getVersionRange().toString());
        }
        if (super.getScope() != null) {
            sb.append(delimiter);
            sb.append(getScope());
        }

        if (creationDate != null) {
            sb.append(delimiter);
            sb.append(getLabelDate(creationDate));
        }

        return sb.toString();
    }

    private void appendArtifactTypeClassifierString(char delimiter, StringBuffer sb) {
        sb.append(getArtifactId());
        sb.append(delimiter);
        sb.append(getType());
        if (hasClassifier()) {
            sb.append(delimiter);
            sb.append(getClassifier());
        }
    }

    private String getLabelDate(Date creationDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(creationDate);
    }

    public void log2Csv(Writer writer) {
        if (getGroupId() != null && !getGroupId().startsWith("fr.soleil.device")) {
            try {
                writer.append("\n");
                writer.append(toCsvLine());
            } catch (IOException ioe) {
                throw new MavenDependencyPluginException(ioe);
            }
        }
    }

    private String toCsvLine() {
        return toString(CSV_DELIMITER);
    }
}
