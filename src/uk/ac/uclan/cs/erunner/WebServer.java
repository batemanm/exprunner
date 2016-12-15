package uk.ac.uclan.cs.erunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpContext;  
import com.sun.net.httpserver.HttpExchange;  
import com.sun.net.httpserver.HttpHandler;  
import com.sun.net.httpserver.HttpServer;  

class WebServer implements HttpHandler, RunnerListener {
private double percent = 0;


public synchronized void logStarted (String command) {}
public synchronized void logFinished (String command, double percent) {
	this.percent = percent;
}
public synchronized void logError (String command) {}


public void handle(HttpExchange t) throws IOException {
	InputStream is = t.getRequestBody();
//	read(is); // .. read the request body

        int d = (int)(percent/10);
	StringBuffer output = new StringBuffer ();
        output.append ("[");
        for (int i = 0; i<d; i ++) {
                output.append ("#");
        }
        for (int i = d; i < 10; i ++) {
                output.append (".");
        }
        output.append ("] " + ((int)percent) + "%");

	String response = output.toString ();
	t.sendResponseHeaders(200, response.length());
	OutputStream os = t.getResponseBody();
	os.write(response.getBytes());
	os.close();
}

public WebServer (int port) {
	try {
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", this);
		server.setExecutor(null); // creates a default executor
		server.start();
	} catch (Exception e) {
		e.printStackTrace ();
	}
}
}
