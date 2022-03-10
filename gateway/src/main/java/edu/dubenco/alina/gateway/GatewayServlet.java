package edu.dubenco.alina.gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

/**
 * This class receives the requests from clients and forwards them to the correct microservice.<br/>
 * It uses {@link ServiceRegistry} for Service Discovery, Load Balancing, Circuit Breaker, etc.
 *  
 * @author Alina Dubenco
 *
 */
@WebServlet(urlPatterns = "/*", loadOnStartup = 1)
public class GatewayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(GatewayServlet.class);
	
	private static final String[] restrictedHeaders = {
	        "Access-Control-Request-Headers",
	        "Access-Control-Request-Method",
	        "Connection",
	        "Content-Length",
	        "Content-Transfer-Encoding",
	        "Host",
	        "Keep-Alive",
	        "Origin",
	        "Trailer",
	        "Transfer-Encoding",
	        "Upgrade",
	        "Via"
	    };
	
	@Value("${http.client.timeout}")
	private long timeoutSeconds; 
	
	@Value("${http.client.threads}")
	private int concurrentThreads;
	
	private HttpClient httpClient;
	
	@Autowired
	ServiceRegistry serviceRegistry;
	
	@Autowired
	CacheClientsPool cacheClientPool;
	
	@PostConstruct
	public void init() {
		httpClient = getHttpClient(timeoutSeconds, concurrentThreads);
//		serviceRegistry.addService("ordering", "http://localhost:81/");
//		serviceRegistry.addService("ordering", "http://localhost:8081/");
//		serviceRegistry.addService("warehouse", "http://localhost:82/");
//		serviceRegistry.addService("warehouse", "http://localhost:8082/");
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String url = request.getPathInfo() + "?" + request.getQueryString();
		
		SimpleHttpResponse simpleResponce = getResponseFromCache(url);
		
		if(simpleResponce == null) {
			LOG.debug("Response for URL '{}' was not found in Cache. Calling microservice.", url);
			simpleResponce = callService(HttpMethod.GET, request, response);
			
			if(simpleResponce.getStatusCode() == 200) {
				addResponseToCache(url, simpleResponce);
			}
		}
		
		sendResponseToClient(simpleResponce, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getPathInfo();
		if(path != null && path.startsWith("/gateway/services")) {
			handleServiceRegistration(request, response);
		} else {
			SimpleHttpResponse simpleResponce = callService(HttpMethod.POST, request, response);
			sendResponseToClient(simpleResponce, response);
		}
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		SimpleHttpResponse simpleResponce = callService(HttpMethod.PUT, request, response);
		sendResponseToClient(simpleResponce, response);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		SimpleHttpResponse simpleResponce = callService(HttpMethod.DELETE, request, response);
		sendResponseToClient(simpleResponce, response);
	}
	
	private void handleServiceRegistration(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		BufferedReader rdr = request.getReader();
		String type = rdr.readLine();
		String error = null;
		if(type == null) {
			error = "Microservice Type has not been specified";
			LOG.warn(error);
		} else {
			String url = rdr.readLine();
			if(url == null) {
				error = "Microservice Type has not been specified";
				LOG.warn(error);
			} else {
				serviceRegistry.addService(type, url);
			}
		}
		if(error != null) {
			response.sendError(400, error);
		}
	}

	private SimpleHttpResponse callService(HttpMethod method, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getPathInfo();
		if(path != null && path.startsWith("/")) {
			path = path.substring(1);
		}
		
		int serviceTypeEndIdx = path.length();
		if(path.contains("/")) {
			serviceTypeEndIdx = path.indexOf("/");
		} else if(path.contains("?")) {
			serviceTypeEndIdx = path.indexOf("?");
		}
		
		String serviceType = path.substring(0, serviceTypeEndIdx);
		
		ServiceInfo si = serviceRegistry.getServiceInfo(serviceType);

		while(si != null) {
			String serviceUrl = si.getUrl();
			String query = request.getQueryString();
			String uri = serviceUrl + path;
			if(query != null) {
				uri = uri + "?" + query;
			}
			String[] headers = getRequestHeaders(request);
	
			InputStream requestBodyInputStream = request.getInputStream();
			
			HttpRequest outboundHttpRequest = buildOutboundRequest(method, uri, headers, requestBodyInputStream);
			
			LOG.debug("Calling microservice of type: '{}', URI: '{}'", serviceType, uri);
			SimpleHttpResponse simpleResponse;
	        
			try {
				HttpResponse<String> outboundHttpResponse = httpClient.send(outboundHttpRequest, HttpResponse.BodyHandlers.ofString());
				
				if(outboundHttpResponse.statusCode() == 500) {
					throw new IOException(outboundHttpResponse.body());
				}
				simpleResponse = new SimpleHttpResponse(outboundHttpResponse);
		        
		        return simpleResponse;
		        
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				simpleResponse = new SimpleHttpResponse(500, "The forwarding has been interrupted.");
		        
		        return simpleResponse;

			} catch (IOException ex) {
				LOG.error("Failed to call microservice of type: '{}', URI: {}. Will try another instance (if available). Error: {}", serviceType, uri, ex.getMessage());
				serviceRegistry.handleCircuitBreakingLogic(si);
				// try another service instance
				si = serviceRegistry.getServiceInfo(serviceType);
			}
		}
		LOG.error("There are no healthy microservices of type '{}'", serviceType);
		return new SimpleHttpResponse(500, "There are no healthy microservices of type '" + serviceType + "'");
	}

	private HttpRequest buildOutboundRequest(HttpMethod method, String uri, String[] headers,
			InputStream requestBodyInputStream) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
		        .uri(URI.create(uri))
		        .headers(headers);
		
		switch(method) {
		case POST:
			builder = builder.POST(HttpRequest.BodyPublishers.ofInputStream(() -> requestBodyInputStream));
			break;
		case PUT:
			builder = builder.PUT(HttpRequest.BodyPublishers.ofInputStream(() -> requestBodyInputStream));
			break;
		case DELETE:
			builder = builder.DELETE();
		default:
			builder = builder.GET();
		}
		
		HttpRequest internalRequest = builder.build();
		return internalRequest;
	}

	private String[] getRequestHeaders(HttpServletRequest request) {
		List<String> headersNames = new ArrayList<>();
		request.getHeaderNames().asIterator().forEachRemaining(hn -> headersNames.add(hn));

		List<String> headersList = new ArrayList<>();
		headersNames.stream()
		.filter(hn -> !isRestrictedHttpHeader(hn))
		.forEach(hn -> {
			String hv = request.getHeader(hn);
			headersList.add(hn);
			headersList.add(hv);
		});
		String[] headers = new String[headersList.size()];
		headers = headersList.toArray(headers);
		return headers;
	}
	
	private void sendResponseToClient(SimpleHttpResponse simpleResponce, HttpServletResponse response) throws IOException {
        response.setStatus(simpleResponce.getStatusCode());
        simpleResponce.getHeaders().forEach((key, valArray) -> {
			if(!isRestrictedHttpHeader(key)) {
				valArray.forEach(val -> response.setHeader(key, val));
			}
        });
        PrintWriter out = response.getWriter();
        out.print(simpleResponce.getBody());
	}
	
	public static boolean isRestrictedHttpHeader(String headerName) {
		for(String restrictedHeader : restrictedHeaders) {
			if(restrictedHeader.equalsIgnoreCase(headerName)) {
				return true;
			}
		}
		return false;
	}

	private SimpleHttpResponse getResponseFromCache(String url) {
		SimpleHttpResponse simpleResponce = null;
		CacheClient cacheClient = null;
		try {
			cacheClient = cacheClientPool.borrowClient();
			if(cacheClient != null) {
				simpleResponce = cacheClient.getCache(url);
			}
		} finally {
			cacheClientPool.returnClient(cacheClient);
		}
		return simpleResponce;
	}

	private void addResponseToCache(String url, SimpleHttpResponse simpleResponce) {
		CacheClient cacheClient = null;
		try {
			cacheClient = cacheClientPool.borrowClient();
			if(cacheClient != null) {
				cacheClient.addCache(url, simpleResponce);
			}
		} finally {
			cacheClientPool.returnClient(cacheClient);
		}
	}

	private HttpClient getHttpClient(long timeoutSeconds, int concurrentThreads) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .executor(Executors.newFixedThreadPool(concurrentThreads))
                .build();
    }

}
