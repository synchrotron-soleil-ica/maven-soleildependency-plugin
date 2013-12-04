package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.repository.mongodb;

import com.mongodb.*;
import fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.repository.MetadataRepository;
import org.apache.maven.artifact.Artifact;

import java.util.Date;

/**
 * @author Gregory Boissinot
 */
public class MongoDBMetadataRepository implements MetadataRepository {

    private static final String MONGODB_ARTIFACTS_COLLECTION = "artifacts";

    private final MongoDBDataSource mongoDBDatasource;

    public MongoDBMetadataRepository(MongoDBDataSource mongoDBDatasource) {
        this.mongoDBDatasource = mongoDBDatasource;
    }

    public Date getCreationDate(Artifact artifact) {
        Date creationDate = null;

        DBCursor cursor = null;
        try {
            DB mongoDB = mongoDBDatasource.getMongoDB();

            DBCollection coll = mongoDB.getCollection(MONGODB_ARTIFACTS_COLLECTION);
            BasicDBObject doc =
                    new BasicDBObject("org", artifact.getGroupId())
                            .append("name", artifact.getArtifactId())
                            .append("type", "binary")
                            .append("status", "RELEASE")
                            .append("version", artifact.getVersion());

            cursor = coll.find(doc);
            while (cursor.hasNext()) {
                BasicDBObject docCurrent = (BasicDBObject) cursor.next();
                creationDate = docCurrent.getDate("creatdate");
            }

            DBObject err = mongoDB.getLastError();
            if (!((CommandResult) err).ok()) {
                throw ((CommandResult) err).getException();
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return creationDate;
    }
}
