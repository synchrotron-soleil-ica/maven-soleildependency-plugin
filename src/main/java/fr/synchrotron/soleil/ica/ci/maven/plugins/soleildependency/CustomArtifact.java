package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;

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


    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (getGroupId() != null) {
            sb.append(getGroupId());
            sb.append(":");
        }
        appendArtifactTypeClassifierString(sb);
        sb.append(":");
        if (getBaseVersionInternal() != null) {
            sb.append(getBaseVersionInternal());
        } else {
            sb.append(super.getVersionRange().toString());
        }
        if (super.getScope() != null) {
            sb.append(":");
            sb.append(getScope());
        }

        if (creationDate != null) {
            sb.append(":");
            sb.append(getLabelDate(creationDate));
        }

        return sb.toString();
    }

    private void appendArtifactTypeClassifierString(StringBuffer sb) {
        sb.append(getArtifactId());
        sb.append(":");
        sb.append(getType());
        if (hasClassifier()) {
            sb.append(":");
            sb.append(getClassifier());
        }
    }

    private String getLabelDate(Date creationDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(creationDate);
    }

}
