package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.service;

import fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.repository.MetadataRepository;
import org.apache.maven.artifact.Artifact;

import java.util.Date;

/**
 * @author Gregory Boissinot
 */
public class MetadataRetrieverService {

    private MetadataRepository metadataRepository;

    public MetadataRetrieverService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public Date getCreationDate(Artifact artifact) {
        return metadataRepository.getCreationDate(artifact);
    }
}
