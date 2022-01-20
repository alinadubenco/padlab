package edu.dubenco.alina.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ServiceRegistry {
	protected HashMap<String, List<ServiceInfo>> servicesMap = new HashMap<>();
	protected HashMap<String, Integer> indexesMap = new HashMap<>();

	public synchronized void addService(String type, String url) {
		ServiceInfo serviceInfo = new ServiceInfo(url);
		
		List<ServiceInfo> serviceList = servicesMap.computeIfAbsent(type, t -> new ArrayList<ServiceInfo>());
		
		if(serviceList.contains(serviceInfo)) {
			serviceList.remove(serviceInfo);
		}
		
		serviceList.add(serviceInfo);
	}
	
	
	public synchronized ServiceInfo getServiceInfo(String type) {
		List<ServiceInfo> serviceList = servicesMap.get(type);
		if(serviceList != null && !serviceList.isEmpty()) {
			int idx = indexesMap.computeIfAbsent(type, t -> Integer.valueOf(-1));
			if(idx >= serviceList.size()) {
				idx = 0;
			}
			
			ServiceInfo si = null;
			int initialIdx = idx;
			
			do {
				idx++;
				if(idx >= serviceList.size()) {
					idx = 0;
				}
				si = serviceList.get(idx);
				indexesMap.put(type, idx);
				
			} while(initialIdx != idx && (si == null || si.getStatus() == ServiceStatus.DOWN));
			
			return si;
		}
		
		return null;
	}
}
