package edu.dubenco.alina.ordering;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

@Configuration
@EnableCouchbaseRepositories(basePackages = {"edu.dubenco.alina.ordering.repo"})
public class ReactiveCouchbaseConfiguration extends AbstractCouchbaseConfiguration {
	
	private String connectionString = "couchbase://127.0.0.1";
	private String userName = "Administrator";
	private String password = "Administrator";
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