package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.service;

import fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.domain.CustomArtifact;
import org.apache.maven.artifact.Artifact;

import java.util.Date;

/**
 * @author Gregory Boissinot
 */
public class ArtifactCustomCreatorService {

    private MetadataRetrieverService retrieverService;

    public ArtifactCustomCreatorService(MetadataRetrieverService retrieverService) {
        this.retrieverService = retrieverService;
    }

    public Artifact replaceArtifact(Artifact artifact) {

        final CustomArtifact customArtifact = new CustomArtifact(artifact);
        final Date creationDate = retrieverService.getCreationDate(artifact);
        if (creationDate != null) {
            customArtifact.setCreationDate(creationDate);
        }
        return customArtifact;
    }
}
