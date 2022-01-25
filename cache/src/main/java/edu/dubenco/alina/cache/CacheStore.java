package edu.dubenco.alina.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 
 * This class implements the Cache operations.
 * 
 * @author Alina Dubenco
 *
 */
public class CacheStore {

    private Cache<String, String> cache;
    
    private long expiryDurationSeconds;
    
    public CacheStore(long expiryDurationSeconds) {
    	this.expiryDurationSeconds = expiryDurationSeconds;
    	init();
    }
    
    public void init() {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDurationSeconds, TimeUnit.SECONDS)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }
	
    public String get(String key) {
        return cache.getIfPresent(key);
    }

    public void add(String key, String value) {
        if(key != null && value != null) {
            cache.put(key, value);
        }
    }
}
