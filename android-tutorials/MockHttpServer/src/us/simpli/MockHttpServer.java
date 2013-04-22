package us.simpli;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.util.Log;


/**
 * This is a sample mock http server for Android.  The mock server can be used in testing situation
 * where there's a need to test HTTP interaction between client (android device) and a backend server (MockHttpServer).
 * The code uses the provided Android HttpClient implementation. 
 * <h4>Limitations</h4>
 * The mock server is single threaded.  The server thread that handles the incoming connection also handle serving the HTTP request.
 * So, if you are looking for performance test to see how your android client behaves, this MockServer would have to be changed to support 
 * multiple threads (sorry, NIO is not supported in current Android's implementation of Apache HttpComponent).
 * 
 * @author Vladimir Vivien
 */
public class MockHttpServer {
	private static String TAG = "MockHttpServer";
	
    private static final String SERVER_NAME = "HttpMockServer/1.0";
    private int port = 9090;
    private int socketDataSize = 4 * 1024;
    private boolean tcpNoDelay = true;
    private int socketDataTimeout = 5000;
    private int socketConnectionTimeout = 2000;
    private volatile boolean started;
    
    private HttpParams params;
    private HttpService httpService;
    private HttpRequestHandlerRegistry registry;
    private BasicHttpProcessor httpproc;
    private ExecutorService executor;
    private HttpServerTask httpServerTask;
    private ServerSocket serverSocket;

    public void setSocketConnectionTimeout(int to) {
        this.socketConnectionTimeout = to;
    }

    public void addRequestHandler(String path, HttpRequestHandler handler) {
        registry.register(path, handler);
    }

    public MockHttpServer(int p) {
        this.port = p;
    }
    
    private void initialize() {
        params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, socketDataTimeout)
              .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, socketConnectionTimeout)
              .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, socketDataSize)
              .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
              .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, tcpNoDelay)
              .setParameter(CoreProtocolPNames.ORIGIN_SERVER, SERVER_NAME);

        httpproc = new BasicHttpProcessor();
        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new ResponseConnControl());

        registry = new HttpRequestHandlerRegistry();
        
        // Set up the HTTP service
        httpService = new HttpService(
            httpproc,
            new DefaultConnectionReuseStrategy(),
            new DefaultHttpResponseFactory()
        );

        httpService.setHandlerResolver(registry);
        httpService.setParams(params);
        
        executor = Executors.newSingleThreadExecutor();
        
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
        	throw new RuntimeException("Unable to bind to port " + port, ex);
        }        
        
        httpServerTask = new HttpServerTask(serverSocket, httpService);
    	executor.execute(httpServerTask);
   }

    public void start() {
    	Log.i(TAG, "Starting HttpMockServer on port " + port);
        if (!started) {
        	initialize();
        	started = true;
        }
    }

    public void stop() {
        if(started){
        	Log.i(TAG, "Stopping HttpMockServer...");
        	
        	try{
        		serverSocket.close();
        		while(!serverSocket.isClosed()){
        			Log.d(TAG, "Waiting to close the server socket...");
        			Thread.sleep(100);
        		}
        		Log.d(TAG, "MockHttpServer socket server closed = " + serverSocket.isClosed());
        	}catch(IOException ex){
				Log.d(TAG, "Unable to close the server socket.... will try again...");
			} catch (InterruptedException e) {
				Log.d(TAG, "Failed while waiting to close socket.");
			}
        	
			try {
	        	// shutodown executor service
	        	executor.shutdown();
				if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
					executor.shutdownNow();
				}
				if(executor.isShutdown() || executor.isTerminated()){
					Log.i(TAG, "HttpMockServer stopped OK.");
				}
			} catch (InterruptedException ie) {
				Log.d(TAG, "Unable to stop HttpMockServer thread ... trying again.");
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
        }
    }
    
    public boolean isStarted() {
    	return started;
    }
    
    class HttpServerTask implements Runnable{
    	ServerSocket serverSocket;
    	private HttpService httpService;
    	
    	public HttpServerTask(final ServerSocket serverSocket, final HttpService svc){
    		this.serverSocket = serverSocket;
    		httpService = svc;
    	}
    	    	
        @Override
        public void run() {
            try {
                while (!serverSocket.isClosed()){
                	final Socket socket = serverSocket.accept();
                	final DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                	conn.bind(socket, httpService.getParams() );
					while(conn.isOpen()){
                    	httpService.handleRequest(conn, new BasicHttpContext(null));
					}                	
                }
            }catch(HttpException ex){
            	throw new RuntimeException(ex);
            }
            catch (IOException ex) {
            	throw new RuntimeException(ex);
            }

        }
    }    
}