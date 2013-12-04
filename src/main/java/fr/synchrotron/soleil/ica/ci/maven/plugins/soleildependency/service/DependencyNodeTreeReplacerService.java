package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.service;

import fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.domain.CustomArtifact;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;

import java.io.Writer;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class DependencyNodeTreeReplacerService {

    private ArtifactCustomCreatorService artifactCustomCreatorService;

    public DependencyNodeTreeReplacerService(ArtifactCustomCreatorService artifactCustomCreatorService) {
        this.artifactCustomCreatorService = artifactCustomCreatorService;
    }

    public DependencyNode replaceDependencyNode(Writer writer, DependencyNode dependencyNode) {

        Artifact newArtifact = artifactCustomCreatorService.replaceArtifact(dependencyNode.getArtifact());
        if (newArtifact instanceof CustomArtifact) {
            CustomArtifact customArtifact = (CustomArtifact) newArtifact;
            customArtifact.log2Csv(writer);
        }

        DependencyNode newDependencyNode = new DependencyNode(newArtifact);
        final List childs = dependencyNode.getChildren();
        if (childs.size() != 0) {
            for (int i = 0; i < childs.size(); i++) {
                DependencyNode curDependencyNode = (DependencyNode) childs.get(i);
                newDependencyNode.addChild(replaceDependencyNode(writer, curDependencyNode));
            }
        }

        return newDependencyNode;
    }


}
