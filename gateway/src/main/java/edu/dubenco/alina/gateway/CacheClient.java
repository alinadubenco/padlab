package edu.dubenco.alina.gateway;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This method implements the client side of the communication with the Cache.
 * 
 * @author Alina Dubenco
 *
 */
public class CacheClient {
	private static final Logger LOG = LoggerFactory.getLogger(CacheClient.class);
	
	private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
	
    private String host;

    private int port;
    
    public CacheClient(String host, int port) {
    	this.host = host;
    	this.port = port;
    }
    
    /**
     * This method establishes the connection with the Cache
     */
    @PostConstruct
    public void startConnection() {
        try {
			clientSocket = new Socket(host, port);
	        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        out = new PrintWriter(clientSocket.getOutputStream(), true);
	        
	        LOG.info("CacheClient has connected to Cache");
		} catch (Exception e) {
			LOG.error("Failed to connect to Cache.", e);
			
			safeClose(clientSocket);
			safeClose(in);
			safeClose(out);
		}
    }

    /**
     * This method is used to add a response to the Cache.
     * @param url - the url for which the response has been received
     * @param httpResponse - the response to be cached
     */
    public void addCache(String url, SimpleHttpResponse httpResponse) {
        out.println("-=|{ADD_CACHE}|=-");
        out.println(url);
        out.println("-=|{HEADERS}|=-");
        httpResponse.getHeaders().forEach((key, valArray) -> {
			if(!GatewayServlet.isRestrictedHttpHeader(key)) {
				valArray.forEach(val -> out.println(key + "||||" + val));
			}
        });
        out.println("-=|{BODY}|=-");
        out.println(httpResponse.getBody());
        out.println("-=|{END}|=-");
        
		try {
			String resp = in.readLine();
			if(!"-=|{ADD_SUCCESS}|=-".equals(resp)) {
				throw new RuntimeException("Wrong acknowledgement from Cache: " + resp);
			}
			LOG.debug("The response for '{}' has been added to Cache", url);
		} catch (IOException e) {
			LOG.error("Failed to get acknowledgement from Cache", e);
		}
    }
    
    /**
     * This method is used to retrieve the cached response for the specified url.
     * @param url - the url whose cached response should be retrieved
     * @return the response or null (if the response for the specified url is not available in Cache)
     */
    public SimpleHttpResponse getCache(String url) {
        SimpleHttpResponse simpleResponse = new SimpleHttpResponse(200, null);
    	try {
	        out.println("-=|{GET_CACHE}|=-");
	        out.println(url);
	        StringBuilder body = new StringBuilder();
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	            if ("-=|{NOT_FOUND}|=-".equals(inputLine)) {
	            	simpleResponse = null;
	            	LOG.debug("Response for '{}' was not found in Cache", url);
	            	break;
	            } else if("-=|{END}|=-".equals(inputLine)) {
	            	simpleResponse.setBody(body.toString());
	            	LOG.debug("Response for '{}' was retrieved from Cache", url);
	            	break;
	            } else if("-=|{HEADERS}|=-".equals(inputLine)){
	            	readHeaders(simpleResponse);
	            } else {
	            	body.append(inputLine).append("\n");
	            }
	        }
    	} catch (Exception e) {
    		LOG.error("Failed to get cache for '" + url + "'", e);
    		simpleResponse = null;
    	}
        
        return simpleResponse;
    }
    
    /**
     * Closes the connection to the Cache
     */
    public void stopConnection() {
		safeClose(clientSocket);
		safeClose(in);
		safeClose(out);
    }
    
    private void readHeaders(SimpleHttpResponse simpleResponse ) throws IOException {
        String inputLine;
    	while ((inputLine = in.readLine()) != null) {
            if ("-=|{BODY}|=-".equals(inputLine)) {
                break;
            } else {
            	String[] parts = inputLine.split("\\|\\|\\|\\|");
            	if(parts.length > 1) {
            		simpleResponse.addHeader(parts[0], parts[1]);
            	} else {
            		LOG.warn("Wrong header line : " + inputLine);
            	}
            }
        }
    }

    private void safeClose(Closeable closable) {
    	if(closable == null) {
    		return;
    	}
    	try {
			closable.close();
		} catch (IOException e) {
			LOG.warn("Exception caught when closing connection", e);
		}
    }
    
}
