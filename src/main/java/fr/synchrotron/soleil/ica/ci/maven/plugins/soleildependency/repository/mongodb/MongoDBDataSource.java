package fr.synchrotron.soleil.ica.ci.maven.plugins.soleildependency.repository.mongodb;

import com.mongodb.DB;

/**
 * @author Gregory Boissinot
 */
public interface MongoDBDataSource {

    public abstract DB getMongoDB();
}