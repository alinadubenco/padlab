package edu.dubenco.alina.gateway;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class acts as a Service Registry: <br/>
 * - it handles Service Discovery (see {@link #addService(String, String)}) <br/>
 * - it holds information about all the services <br/>
 * - it is used to do the Load Balancing between the service instances (see {@link #getServiceInfo(String)}) <br/>
 * - it handles the Circuit Breaker Logic (see {@link #handleCircuitBreakingLogic(ServiceInfo)}, {@link #getServiceInfo(String)}, 
 * 		{@link #getNextServiceInfo(String)} and {@link #refreshServiceStatus(ServiceInfo)}) <br/>
 * 
 * @author Alina Dubenco
 *
 */
@Service
public class ServiceRegistry {
	
	private static Logger LOG = LoggerFactory.getLogger(ServiceRegistry.class);
	
	private int failuresThreshold = 3;
	private long circuitResetTimeout = 60 * 1000; //1 minute
	
	private HashMap<String, List<ServiceInfo>> servicesMap = new HashMap<>();
	private HashMap<String, Integer> indexesMap = new HashMap<>();

	HttpClient httpClient = HttpClient.newBuilder()
			    .version(HttpClient.Version.HTTP_1_1)
			    .connectTimeout(Duration.ofMillis(500))
			    .executor(Executors.newFixedThreadPool(1))
			    .build();

	/**
	 * Adds a service instance to the registry
	 * 
	 * @param type - type of the service
	 * @param url  - the base URL of the service
	 */
	public synchronized void addService(String type, String url) {
		ServiceInfo serviceInfo = new ServiceInfo(url);
		
		List<ServiceInfo> serviceList = servicesMap.computeIfAbsent(type, t -> new ArrayList<ServiceInfo>());
		
		if(serviceList.contains(serviceInfo)) {
			serviceList.remove(serviceInfo);
		}
		
		serviceList.add(serviceInfo);
		
		LOG.info("Service registered: type=" + type + " URL=" + url);
	}
	
	/**
	 * Get the information about a service instance of the specified type.<br/>
	 * The service returned by this method (if not null) is guaranteed to have status CircuitBreakerStatus.CLOSED
	 * 
	 * @param type - the type of the service
	 * @return a ServiceInfo (having status CircuitBreakerStatus.CLOSED) 
	 * 			or null (if there are no available services of the specified type)
	 */
	public ServiceInfo getServiceInfo(String type) {
		ServiceInfo si = getNextServiceInfo(type);
		while(si != null) {
			if(si.getStatus() == CircuitBreakerStatus.HALF_OPEN) {
				refreshServiceStatus(si);
			}
			if(si.getStatus() == CircuitBreakerStatus.OPEN) {
				si = getNextServiceInfo(type);
			} else {
				break;
			}
		}
		return si;
	}

	/**
	 * This method handles the circuit breaking logic for the specified service instance.<br/>
	 * This method is called whenever the call to a service instance has failed.
	 * 
	 * @param si - information about the service instance
	 */
	public void handleCircuitBreakingLogic(ServiceInfo si) {
		si.setFailureTime(new Date());
		si.setFailuresCount(si.getFailuresCount() + 1);
		if(si.getFailuresCount() >= failuresThreshold) {
			si.setStatus(CircuitBreakerStatus.OPEN);
		}
	}
	
	/**
	 * Get the information about the next available service for the specified type using Round Robin method.<br/>
	 * This method skips the services that have status CircuitBreakerStatus.OPEN.
	 * 
	 * @param type - the type of the service
	 * @return a ServiceInfo (having status either CircuitBreakerStatus.CLOSED or CircuitBreakerStatus.HALF_OPEN) 
	 * 			or null (if there are no available services of the specified type)
	 */
	private synchronized ServiceInfo getNextServiceInfo(String type) {
		List<ServiceInfo> serviceList = servicesMap.get(type);
		ServiceInfo si = null;
		if(serviceList != null && !serviceList.isEmpty()) {
			int idx = indexesMap.computeIfAbsent(type, t -> Integer.valueOf(0));
			if(idx >= serviceList.size()) {
				idx = 0;
			}
			
			int initialIdx = idx;
			
			do {
				si = serviceList.get(idx);

				idx++;
				if(idx >= serviceList.size()) {
					idx = 0;
				}
				indexesMap.put(type, idx);
				
				if(si.getStatus() == CircuitBreakerStatus.OPEN) {

					long now = (new Date()).getTime();
					long millisSinceLastFailure = now - si.getFailureTime().getTime();
					
					if(millisSinceLastFailure > circuitResetTimeout) {
						si.setStatus(CircuitBreakerStatus.HALF_OPEN);
					}
				}
				
			} while(initialIdx != idx && (si == null || si.getStatus() == CircuitBreakerStatus.OPEN));
			
			if(si.getStatus() == CircuitBreakerStatus.OPEN) {
				si = null;
			}
		}
		return si;
	}
	
	/**
	 * Refresh the circuit breaker status of a service by calling its "status" endpoint
	 * 
	 * @param si - information about the service instance
	 */
	private void refreshServiceStatus(ServiceInfo si) {
		String uri = si.getUrl() + "status";
		HttpRequest request = HttpRequest.newBuilder()
			        .uri(URI.create(uri))
			        .GET()
			        .build();
		try {
			httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			si.setStatus(CircuitBreakerStatus.CLOSED);
		} catch (IOException e) {
			LOG.info("Health check for service URL " + uri + " has failed.");
			si.setStatus(CircuitBreakerStatus.OPEN);
		} catch (InterruptedException e) {
			LOG.info("Health check has been interrupted.");
			Thread.currentThread().interrupt();
		}
	}
}
