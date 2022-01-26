package edu.dubenco.alina.cache;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main class of the Cache application
 * .
 * @author Alina Dubenco
 *
 */
public class CacheApplication {
	private static final Logger LOG = LoggerFactory.getLogger(CacheApplication.class);

    private ServerSocket serverSocket;
    private ExecutorService executor;
    private CacheStore cacheStore;
	
	public static void main(String[] args) throws IOException {
		new CacheApplication().start();
	}
	
	public void start() throws IOException {
		int port = getSystemPropertyAsInt("server.port", 8081);
		int maxThreads = getSystemPropertyAsInt("max.threads", 10);
		long expiryDurationSeconds = getSystemPropertyAsInt("expiry.duration.seconds", 120);
		cacheStore = new CacheStore(expiryDurationSeconds);
		serverSocket = new ServerSocket(port);
		LOG.info("Chache server started on port " + port);
		executor = Executors.newFixedThreadPool(maxThreads);
		
		while(true) {
			Socket socket = serverSocket.accept();
			executor.submit(new CommandsHandler(socket, cacheStore));
		}
		
	}
	
	private int getSystemPropertyAsInt(String propertyName, int defaultValue) {
		try {
			return Integer.parseInt(System.getProperty(propertyName));
		} catch (Exception e) {
			LOG.warn("Failed to get value of System property '" + propertyName + "'. Using default value: " + defaultValue);
			return defaultValue;
		}
	}

}
