package us.simpli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.util.Log;

public class MockHttpServerTest extends TestCase {
	private final static int SVR_PORT = 8585;
	private final static String SVR_ADDR = "http://localhost:" + SVR_PORT;
	
	public MockHttpServerTest() {
		super("MockHttpServerTest");
	}
	
	public void testStartStopMockServer(){
		MockHttpServer svr = new MockHttpServer(SVR_PORT);
		svr.start();
		assert svr.isStarted() == true : "MockHttpServer not starting";
		svr.stop();
		assert !svr.isStarted() == true: "MockHttpServer not stopping properly";
	}
	
	public void testHeaderEnumerations () throws Exception{
		// setup mock server and handler for given path
        MockHttpServer server = new MockHttpServer(SVR_PORT);
        server.start();
        
        server.addRequestHandler("/test1", new HttpRequestHandler() {

            @Override
            public void handle(HttpRequest req, HttpResponse rsp, HttpContext context) throws HttpException, IOException {
            	int headerCount = 0;
                String headers = "Accept Accept-Charset";
                HeaderIterator it = req.headerIterator();
                while (it.hasNext()){
                    Header h = (Header) it.next();
                    if(headers.contains(h.getName())){
                        headerCount++;
                    }
                }
                Assert.assertEquals(3, headerCount);
            }
        });
        
        // setup client connection
        URL url = new URL(SVR_ADDR + "/test1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("Accept", "text/xml");
        conn.addRequestProperty("Accept", "Audio/mpeg");
        conn.addRequestProperty("Accept-Charset", "utf-8");
        
        Log.d("MockHttpServer", "((( Resposnce code for /test1 = " + conn.getResponseCode() + ")))");
        
        // make request
        try{
        	conn.getInputStream();
        }finally{
        	conn.disconnect();
        }
        
        server.stop();
	}
	
	public void testStreamFromMockServer() throws Exception {
		
		final String helloWorldResponse = "Hello World";
		
		MockHttpServer server = new MockHttpServer(SVR_PORT);
		server.start();
		
		server.addRequestHandler("/test2", new HttpRequestHandler() {
			@Override
			 public void handle(HttpRequest req, HttpResponse rsp, HttpContext context) throws HttpException, IOException {
				rsp.setStatusCode(HttpStatus.SC_OK);
				StringEntity body = new StringEntity(helloWorldResponse, "UTF-8");
				body.setContentType("text/plain; charset=UTF-8");
				rsp.setEntity(body);
			}
		});
		
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        request.setURI(new URI(SVR_ADDR + "/test2"));
        HttpResponse response = client.execute(request);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Assert.assertEquals(helloWorldResponse, reader.readLine());
        
        reader.close();
        
		Thread.sleep(200);
        server.stop(); 
	}

}
