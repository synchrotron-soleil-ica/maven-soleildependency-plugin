package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.repository.mongodb;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;

/**
 * @author Gregory Boissinot
 */
public class BasicMongoDBDataSource implements MongoDBDataSource {

    private static final String DEFAULT_MONGODB_HOST = "localhost";
    private static final int DEFAULT_MONGODB_PORT = 27017;
    private static final String DEFAULT_MONGODB_DBNAME = "artifactRepository";

    private final MongoClient mongo;
    private final String mongoDBName;

    public BasicMongoDBDataSource() {
        try {
            mongo = new MongoClient(DEFAULT_MONGODB_HOST, DEFAULT_MONGODB_PORT);
        } catch (UnknownHostException ue) {
            throw new MongoDBException(ue);
        }
        this.mongoDBName = DEFAULT_MONGODB_DBNAME;
    }

    public BasicMongoDBDataSource(String mongoHost, int mongoPort, String mongoDBName) {
        try {
            mongo = new MongoClient(mongoHost, mongoPort);
        } catch (UnknownHostException ue) {
            throw new MongoDBException(ue);
        }
        this.mongoDBName = mongoDBName;
    }

    public BasicMongoDBDataSource(String mongoHost, int mongoPort) {
        try {
            mongo = new MongoClient(mongoHost, mongoPort);
        } catch (UnknownHostException ue) {
            throw new MongoDBException(ue);
        }
        this.mongoDBName = DEFAULT_MONGODB_DBNAME;
    }

    public DB getMongoDB() {
        return mongo.getDB(mongoDBName);
    }

}
