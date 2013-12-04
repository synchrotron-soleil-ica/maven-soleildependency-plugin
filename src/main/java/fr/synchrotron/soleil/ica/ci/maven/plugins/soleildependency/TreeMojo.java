package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.repository.mongodb.BasicMongoDBDataSource;
import fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.repository.mongodb.MongoDBMetadataRepository;
import fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.service.MetadataRetrieverService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.Restriction;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.RuntimeInformation;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.StrictPatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.StrictPatternIncludesArtifactFilter;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.filter.*;
import org.apache.maven.shared.dependency.tree.traversal.*;
import org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor.TreeTokens;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Displays the dependency tree for this project.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: TreeMojo.java 728546 2008-12-21 22:56:51Z bentmann $
 * @goal tree
 * @requiresDependencyResolution test
 * @since 2.0-alpha-5
 */
public class TreeMojo extends AbstractMojo {
    // fields -----------------------------------------------------------------

    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The artifact repository to use.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The artifact factory to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * The artifact metadata source to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * The artifact collector to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;

    /**
     * The dependency tree builder to use.
     *
     * @component
     * @required
     * @readonly
     */
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * If specified, this parameter will cause the dependency tree to be written to the path specified, instead of
     * writing to the console.
     *
     * @parameter expression="${output}"
     * @deprecated use outputFile instead.
     */
    private File output;

    /**
     * If specified, this parameter will cause the dependency tree to be written to the path specified, instead of
     * writing to the console.
     *
     * @parameter expression="${outputFile}"
     * @since 2.0-alpha-5
     */
    private File outputFile;

    /**
     * The scope to filter by when resolving the dependency tree, or <code>null</code> to include dependencies from
     * all scopes. Note that this feature does not currently work due to MNG-3236.
     *
     * @parameter expression="${scope}"
     * @see <a href="http://jira.codehaus.org/browse/MNG-3236">MNG-3236</a>
     * @since 2.0-alpha-5
     */
    private String scope;

    /**
     * Whether to include omitted nodes in the serialized dependency tree.
     *
     * @parameter expression="${verbose}" default-value="false"
     * @since 2.0-alpha-6
     */
    private boolean verbose;

    /**
     * The token set name to use when outputting the dependency tree. Possible values are <code>whitespace</code>,
     * <code>standard</code> or <code>extended</code>, which use whitespace, standard or extended ASCII sets
     * respectively.
     *
     * @parameter expression="${tokens}" default-value="standard"
     * @since 2.0-alpha-6
     */
    private String tokens;

    /**
     * A comma-separated list of artifacts to filter the serialized dependency tree by, or <code>null</code> not to
     * filter the dependency tree. The artifact syntax is defined by <code>StrictPatternIncludesArtifactFilter</code>.
     *
     * @parameter expression="${includes}"
     * @see StrictPatternIncludesArtifactFilter
     * @since 2.0-alpha-6
     */
    private String includes;

    /**
     * A comma-separated list of artifacts to filter from the serialized dependency tree, or <code>null</code> not to
     * filter any artifacts from the dependency tree. The artifact syntax is defined by
     * <code>StrictPatternExcludesArtifactFilter</code>.
     *
     * @parameter expression="${excludes}"
     * @see StrictPatternExcludesArtifactFilter
     * @since 2.0-alpha-6
     */
    private String excludes;

    /**
     * Runtime Information used to check the Maven version
     *
     * @component role="org.apache.maven.execution.RuntimeInformation"
     * @since 2.0
     */
    private RuntimeInformation rti;

    /**
     * The computed dependency tree root node of the Maven project.
     */
    private DependencyNode rootNode;

    // Mojo methods -----------------------------------------------------------

    /*
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        ArtifactVersion detectedMavenVersion = rti.getApplicationVersion();
        VersionRange vr;
        try {
            vr = VersionRange.createFromVersionSpec("[2.0.8,)");
            if (!containsVersion(vr, detectedMavenVersion)) {
                getLog().warn(
                        "The tree mojo requires at least Maven 2.0.8 to function properly. You may get eroneous results on earlier versions");
            }
        } catch (InvalidVersionSpecificationException e) {
            throw new MojoExecutionException(e.getLocalizedMessage());
        }


        if (output != null) {
            getLog().warn("The parameter output is deprecated. Use outputFile instead.");
            this.outputFile = output;
        }

        ArtifactFilter artifactFilter = createResolvingArtifactFilter();

        try {
            // TODO: note that filter does not get applied due to MNG-3236

            rootNode =
                    dependencyTreeBuilder.buildDependencyTree(project, localRepository, artifactFactory,
                            artifactMetadataSource, artifactFilter, artifactCollector);

            String dependencyTreeString = serialiseDependencyTree(rootNode);

            if (outputFile != null) {
                DependencyUtil.write(dependencyTreeString, outputFile, getLog());

                getLog().info("Wrote dependency tree to: " + outputFile);
            } else {
                DependencyUtil.log(dependencyTreeString, getLog());
            }
        } catch (DependencyTreeBuilderException exception) {
            throw new MojoExecutionException("Cannot build project dependency tree", exception);
        } catch (IOException exception) {
            throw new MojoExecutionException("Cannot serialise project dependency tree", exception);
        }
    }

    // public methods ---------------------------------------------------------

    /**
     * Gets the Maven project used by this mojo.
     *
     * @return the Maven project
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * Gets the computed dependency tree root node for the Maven project.
     *
     * @return the dependency tree root node
     */
    public DependencyNode getDependencyTree() {
        return rootNode;
    }

    // private methods --------------------------------------------------------

    /**
     * Gets the artifact filter to use when resolving the dependency tree.
     *
     * @return the artifact filter
     */
    private ArtifactFilter createResolvingArtifactFilter() {
        ArtifactFilter filter;

        // filter scope
        if (scope != null) {
            getLog().debug("+ Resolving dependency tree for scope '" + scope + "'");

            filter = new ScopeArtifactFilter(scope);
        } else {
            filter = null;
        }

        return filter;
    }


    private Artifact replaceArtifact(MetadataRetrieverService retrieverService, Artifact artifact) {

        final CustomArtifact customArtifact = new CustomArtifact(artifact);
        final Date creationDate = retrieverService.getCreationDate(artifact);
        if (creationDate != null) {
            customArtifact.setCreationDate(creationDate);
        }
        return customArtifact;

    }

    private DependencyNode replaceDependencyNode(MetadataRetrieverService retrieverService, DependencyNode dependencyNode) {

        Artifact newArtifact = replaceArtifact(retrieverService, dependencyNode.getArtifact());
        DependencyNode newDependencyNode = new DependencyNode(newArtifact);

        final List childs = dependencyNode.getChildren();
        if (childs.size() != 0) {
            for (int i = 0; i < childs.size(); i++) {
                DependencyNode curDependencyNode = (DependencyNode) childs.get(i);
                newDependencyNode.addChild(replaceDependencyNode(retrieverService, curDependencyNode));
            }
        }
        return newDependencyNode;
    }

    /**
     * Serialises the specified dependency tree to a string.
     *
     * @param rootNode the dependency tree root node to serialise
     * @return the serialised dependency tree
     */
    private String serialiseDependencyTree(DependencyNode rootNode) {

        MetadataRetrieverService retrieverService = new MetadataRetrieverService(
                new MongoDBMetadataRepository(
                        new BasicMongoDBDataSource("localhost", 27017, "repo")));

        DependencyNode newRootNode = replaceDependencyNode(retrieverService, rootNode);


        StringWriter writer = new StringWriter();
        TreeTokens treeTokens = toTreeTokens(tokens);

        DependencyNodeVisitor visitor = new SerializingDependencyNodeVisitor(writer, treeTokens);

        visitor = new BuildingDependencyNodeVisitor(visitor);

        DependencyNodeFilter filter = createDependencyNodeFilter();

        if (filter != null) {
            CollectingDependencyNodeVisitor collectingVisitor = new CollectingDependencyNodeVisitor();
            DependencyNodeVisitor firstPassVisitor = new FilteringDependencyNodeVisitor(collectingVisitor, filter);
            newRootNode.accept(firstPassVisitor);

            DependencyNodeFilter secondPassFilter = new AncestorOrSelfDependencyNodeFilter(collectingVisitor.getNodes());
            visitor = new FilteringDependencyNodeVisitor(visitor, secondPassFilter);
        }

        newRootNode.accept(visitor);

        return writer.toString();
    }

    /**
     * Gets the tree tokens instance for the specified name.
     *
     * @param tokens the tree tokens name
     * @return the <code>TreeTokens</code> instance
     */
    private TreeTokens toTreeTokens(String tokens) {
        TreeTokens treeTokens;

        if ("whitespace".equals(tokens)) {
            getLog().debug("+ Using whitespace tree tokens");

            treeTokens = SerializingDependencyNodeVisitor.WHITESPACE_TOKENS;
        } else if ("extended".equals(tokens)) {
            getLog().debug("+ Using extended tree tokens");

            treeTokens = SerializingDependencyNodeVisitor.EXTENDED_TOKENS;
        } else {
            treeTokens = SerializingDependencyNodeVisitor.STANDARD_TOKENS;
        }

        return treeTokens;
    }

    /**
     * Gets the dependency node filter to use when serializing the dependency tree.
     *
     * @return the dependency node filter, or <code>null</code> if none required
     */
    private DependencyNodeFilter createDependencyNodeFilter() {
        List filters = new ArrayList();

        // filter node states
        if (!verbose) {
            getLog().debug("+ Filtering omitted nodes from dependency tree");

            filters.add(StateDependencyNodeFilter.INCLUDED);
        }

        // filter includes
        if (includes != null) {
            List patterns = Arrays.asList(includes.split(","));

            getLog().debug("+ Filtering dependency tree by artifact include patterns: " + patterns);

            ArtifactFilter artifactFilter = new StrictPatternIncludesArtifactFilter(patterns);
            filters.add(new ArtifactDependencyNodeFilter(artifactFilter));
        }

        // filter excludes
        if (excludes != null) {
            List patterns = Arrays.asList(excludes.split(","));

            getLog().debug("+ Filtering dependency tree by artifact exclude patterns: " + patterns);

            ArtifactFilter artifactFilter = new StrictPatternExcludesArtifactFilter(patterns);
            filters.add(new ArtifactDependencyNodeFilter(artifactFilter));
        }

        return filters.isEmpty() ? null : new AndDependencyNodeFilter(filters);
    }

    //following is required because the version handling in maven code 
    //doesn't work properly. I ripped it out of the enforcer rules.


    /**
     * Copied from Artifact.VersionRange. This is tweaked to handle singular ranges properly. Currently the default
     * containsVersion method assumes a singular version means allow everything. This method assumes that "2.0.4" ==
     * "[2.0.4,)"
     *
     * @param allowedRange range of allowed versions.
     * @param theVersion   the version to be checked.
     * @return true if the version is contained by the range.
     */
    public static boolean containsVersion(VersionRange allowedRange, ArtifactVersion theVersion) {
        boolean matched = false;
        ArtifactVersion recommendedVersion = allowedRange.getRecommendedVersion();
        if (recommendedVersion == null) {

            for (Iterator i = allowedRange.getRestrictions().iterator(); i.hasNext() && !matched; ) {
                Restriction restriction = (Restriction) i.next();
                if (restriction.containsVersion(theVersion)) {
                    matched = true;
                }
            }
        } else {
            // only singular versions ever have a recommendedVersion
            int compareTo = recommendedVersion.compareTo(theVersion);
            matched = (compareTo <= 0);
        }
        return matched;
    }

}
