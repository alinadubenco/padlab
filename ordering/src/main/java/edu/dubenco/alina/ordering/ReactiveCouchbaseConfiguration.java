package edu.dubenco.alina.ordering;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

/**
 * This class is used for connecting to Couchbase DB
 * 
 * @author Alina Dubenco
 *
 */
@Configuration
@EnableCouchbaseRepositories(basePackages = {"edu.dubenco.alina.ordering.repo"})
public class ReactiveCouchbaseConfiguration extends AbstractCouchbaseConfiguration {
	
	@Value("${couchbase.connection.string}")
	private String connectionString;
	@Value("${couchbase.user}")
	private String userName;
	@Value("${couchbase.password}")
	private String password;
	private String bucketName = "orders";

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

}