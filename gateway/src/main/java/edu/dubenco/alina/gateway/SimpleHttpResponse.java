package edu.dubenco.alina.gateway;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleHttpResponse {
	private int statusCode;
	private Map<String, List<String>> headers = new HashMap<>();
	private String body;
	
	public SimpleHttpResponse(int statusCode, String body) {
		this.statusCode = statusCode;
		this.body = body;
	}
	
	public SimpleHttpResponse(HttpResponse<String> response) {
        this.setStatusCode(response.statusCode());
		response.headers().map().forEach((key, valArray) -> {
			if(!GatewayServlet.isRestrictedHttpHeader(key)) {
				valArray.forEach(val -> this.addHeader(key, val));
			}
        });
		
		this.setBody(response.body());
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public void addHeader(String name, String value) {
		headers.computeIfAbsent(name, n -> new ArrayList<>()).add(value);
	}
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
	
}
