package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.repository;

import org.apache.maven.artifact.Artifact;

import java.util.Date;

/**
 * @author Gregory Boissinot
 */
public interface MetadataRepository {

    public Date getCreationDate(Artifact artifact);
}
