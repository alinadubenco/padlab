package edu.dubenco.alina.gateway;

import java.util.Date;
import java.util.Objects;

public class ServiceInfo {
	private String url;
	private ServiceStatus status;
	private Date failureTime;
	
	public ServiceInfo(String url) {
		this.url = url;
		this.status = ServiceStatus.UP;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public ServiceStatus getStatus() {
		return status;
	}
	public void setStatus(ServiceStatus status) {
		this.status = status;
	}
	public Date getFailureTime() {
		return failureTime;
	}
	public void setFailureTime(Date failureTime) {
		this.failureTime = failureTime;
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
