package edu.dubenco.alina.gateway;

import java.time.Duration;

import javax.annotation.PostConstruct;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This Class is used to create a pool of {@link CacheClient}s. <br/>
 * It is used to borrow a CacheClient using the {@link #borrowClient()} method. 
 * The CacheClient instance returned by this method can be used to send commands to the Cache application. <br/>
 * After a CacheClient was used, it must be returned using {@link #returnClient(CacheClient)} method.
 * 
 * @author Alina Dubenco
 *
 */
@Service
public class CacheClientsPool extends BasePooledObjectFactory<CacheClient>{
	
	private static final Logger LOG = LoggerFactory.getLogger(CacheClientsPool.class);

	@Value("${cache.host}")
    private String host;

    @Value("${cache.port}")
    private int port;
    
    @Value("${cache.client.max.wait.seconds}")
    private int maxWaitSeconds;

    @Value("${cache.client.max.pool.size}")
    private int maxPoolSize;
    
    private GenericObjectPool<CacheClient> pool;
    
    @PostConstruct
    public void init() {
    	pool = new GenericObjectPool<CacheClient>(this);
    	pool.setMaxTotal(maxPoolSize);
    	pool.setBlockWhenExhausted(true);
    	pool.setMaxWait(Duration.ofSeconds(maxWaitSeconds));
    }

    /**
     * This method is used to borrow a CacheClient in order to send commands to the Cache 
     * application for execution. 
     * @return a CacheClient instance
     */
	public CacheClient borrowClient() {
		try {
			return pool.borrowObject();
		} catch (Exception e) {
			LOG.error("Failed to borrow CacheClient from pool");
			return null;
		}
	}
	
	/**
	 * When the CacheClient is no longer needed, the thread must call this method to return the 
	 * instance in order for other threads to be able to use it.
	 * @param client - the instance being returned to the pool
	 */
	public void returnClient(CacheClient client) {
		if(client != null) {
			pool.returnObject(client);
		}
	}

	public void setPool(GenericObjectPool<CacheClient> pool) {
		this.pool = pool;
	}

	@Override
	public CacheClient create() throws Exception {
		CacheClient cacheClient = new CacheClient(host, port);
		cacheClient.startConnection();
		return cacheClient;
	}

	@Override
	public PooledObject<CacheClient> wrap(CacheClient obj) {
		return new DefaultPooledObject<CacheClient>(obj);
	}

}
