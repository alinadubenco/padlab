package edu.dubenco.alina.gateway;

import java.util.Date;
import java.util.Objects;

/**
 * This class hold information about a microservice instance
 * 
 * @author Alina Dubenco
 *
 */
public class ServiceInfo {
	private String url;
	private CircuitBreakerStatus status;
	private Date failureTime;
	private int failuresCount;
	
	public ServiceInfo(String url) {
		this.url = url;
		this.status = CircuitBreakerStatus.CLOSED;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public CircuitBreakerStatus getStatus() {
		return status;
	}
	public void setStatus(CircuitBreakerStatus status) {
		this.status = status;
	}
	public Date getFailureTime() {
		return failureTime;
	}
	public void setFailureTime(Date failureTime) {
		this.failureTime = failureTime;
	}
	public int getFailuresCount() {
		return failuresCount;
	}
	public void setFailuresCount(int failuresCount) {
		this.failuresCount = failuresCount;
	}

	@Override
	public int hashCode() {
		return Objects.hash(url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceInfo other = (ServiceInfo) obj;
		return Objects.equals(url, other.url);
	}
	
	
}
