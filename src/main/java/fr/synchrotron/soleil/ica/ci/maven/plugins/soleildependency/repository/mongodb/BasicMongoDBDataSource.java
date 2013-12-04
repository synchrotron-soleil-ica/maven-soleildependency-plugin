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

    private final String mongoHost;

    private final int mongoPort;

    private final String mongoDBName;

    public BasicMongoDBDataSource() {
        this.mongoHost = DEFAULT_MONGODB_HOST;
        this.mongoPort = DEFAULT_MONGODB_PORT;
        this.mongoDBName = DEFAULT_MONGODB_DBNAME;
    }

    public BasicMongoDBDataSource(String mongoHost, int mongoPort, String mongoDBName) {
        this.mongoHost = mongoHost;
        this.mongoPort = mongoPort;
        this.mongoDBName = mongoDBName;
    }

    public BasicMongoDBDataSource(String mongoHost, int mongoPort) {
        this.mongoHost = mongoHost;
        this.mongoPort = mongoPort;
        this.mongoDBName = DEFAULT_MONGODB_DBNAME;
    }

    public DB getMongoDB() {
        MongoClient mongoClient;
        try {
            mongoClient = new MongoClient(mongoHost, mongoPort);
        } catch (UnknownHostException ue) {
            throw new MongoDBException(ue);
        }
        return mongoClient.getDB(mongoDBName);
    }
}
