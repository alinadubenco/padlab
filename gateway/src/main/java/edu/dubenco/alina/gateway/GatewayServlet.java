package edu.dubenco.alina.gateway;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
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
	
	protected HttpClient httpClient;
	
	@Autowired
	ServiceRegistry serviceRegistry;
	
	@PostConstruct
	public void init() {
		httpClient = getHttpClient(timeoutSeconds, concurrentThreads);
		serviceRegistry.addService("ordering", "http://localhost:8081/");
		serviceRegistry.addService("warehouse", "http://localhost:8082/");
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOG.info("------------- GET ------------------------");
		
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
		//TODO: treat null
		String serviceUrl = si.getUrl();
		String query = request.getQueryString();
		String uri = serviceUrl + path;
		if(query != null) {
			uri = uri + query;
		}
		String[] headers = getRequestHeaders(request);
        
		HttpRequest internalRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri))
                .headers(headers)
                .build();
        
        PrintWriter out = response.getWriter();
        HttpResponse<String> internalResponse;
		try {
			internalResponse = httpClient.send(internalRequest, HttpResponse.BodyHandlers.ofString());
	        response.setStatus(internalResponse.statusCode());
			internalResponse.headers().map().forEach((key, valArray) -> {
				if(!isRestrictedHttpHeader(key)) {
					valArray.forEach(val -> response.setHeader(key, val));
				}
	        });
			
			String body = internalResponse.body();

	        out.print(body);
	        
	        out.flush();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			response.setStatus(500);
	        out.print("The forwarding has been interrupted.");
		}
		
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
	
	private boolean isRestrictedHttpHeader(String headerName) {
		for(String restrictedHeader : restrictedHeaders) {
			if(restrictedHeader.equalsIgnoreCase(headerName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOG.info("------------- POST ------------------------");
		doGet(request, response);
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOG.info("------------- PUT ------------------------");
		doGet(request, response);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOG.info("------------- DELETE ------------------------");
		doGet(request, response);
	}
	
	private HttpClient getHttpClient(long timeoutSeconds, int concurrentThreads) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .executor(Executors.newFixedThreadPool(concurrentThreads))
                .build();
    }

}
