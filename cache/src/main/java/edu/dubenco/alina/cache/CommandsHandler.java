package edu.dubenco.alina.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandsHandler implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(CommandsHandler.class);
	
    private Socket clientSocket;
    private CacheStore cacheStore;
    
    public CommandsHandler(Socket socket, CacheStore cacheStore) {
    	this.clientSocket = socket;
    	this.cacheStore = cacheStore;
    }

	@Override
	public void run() {
		try (
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		) {
        
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	            if ("-=|{CLOSE}|=-".equals(inputLine)) {
	                out.println("-=|{BYE}|=-");
	                break;
	            } else if("-=|{ADD_CACHE}|=-".equals(inputLine)) {
	            	handleAddCache(in, out);
	            } else if("-=|{GET_CACHE}|=-".equals(inputLine)) {
	            	handleGetCache(in, out);
	            } else {
	            	LOG.error("Invalid command: " + inputLine);
	            }
	        }
		} catch (IOException e) {
			LOG.error("Failure wile communicating with client", e);
		} finally {
	        try {
				clientSocket.close();
			} catch (IOException e) {
				LOG.warn("Exception caught while closing client socket", e);
			}
		}

	}
	
	private void handleAddCache(BufferedReader in, PrintWriter out) throws IOException {
		String key = null;
		StringBuilder value = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if ("-=|{END}|=-".equals(inputLine)) {
            	if(key != null) {
            		cacheStore.add(key, value.toString());
            		out.println("-=|{ADD_SUCCESS}|=-");
            	} else {
            		LOG.warn("Key is null. There is nothing to add to cache.");
            	}
                break;
            } else if(key == null) {
            	key = inputLine;
            } else {
            	value.append(inputLine).append("\n");
            }
        }
	}
	
	private void handleGetCache(BufferedReader in, PrintWriter out) throws IOException {
		String inputLine = in.readLine();
		if(inputLine != null) {
			String value = cacheStore.get(inputLine);
			if(value == null) {
				out.println("-=|{NOT_FOUND}|=-");
			} else {
				out.println(value);
				out.println("-=|{END}|=-");
			}
		}
	}
}
