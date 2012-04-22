# MockHttpServer for Android #
While developing an Android project, I needed to test an HTTP client service layer running in my application.  
I wanted to ensure I unit tested features such as ability to send/receive headers, request http resources, and deal with HTTP 
response codes properly.  However, I did not want to run my JUnit tests using an external HTTP server.  This would create a 
dependency on yet another technology that I would have to launch with Maven.  Furthermore, using an external server would make 
it difficult to validate unit test cases from the server side. 

## MockHttpServer ##
You may already know that Android's library comes with a version of Apache's [HttpComponent] (http://hc.apache.org/).  While 
Android promotes the use of HttpComponent for mostly client side development, the provided APIs include the server-side pieces 
as well.  So, you can create your own HTTP server running on Android.

MockHttpServer is a wrapper around the HttpComponent's 
[HttpService API] (http://developer.android.com/reference/org/apache/http/protocol/HttpService.html).
MockHttpServer provides a simple interaction point where developers simply provide an implementation of 
[HttpRequestHandler] (http://developer.android.com/reference/org/apache/http/protocol/HttpRequestHandler.html).  
The MockHttpServer handles the setup and thread management.  The following shows how to use the MockHttpServer
to enumerate and test the expected headers from the client:

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
        
        // make request
        try{
        	conn.getInputStream();
        }finally{
        	conn.disconnect();
        }
        
        server.stop();
	}

The top portion of the code sets up the HttpRequestHandler implementation.  This is where one specifies how the server would handle the 
incoming HTTP request.  In the implementation above, we are enumerating and validating incoming HTTP headers.
The second portion of the code sets up the client call to the MockHttpServer instance.  Here, for simplicity, we are using an 
instance of HttpURLConnection to make the HTTP request to the MockHttpServer.

## Limitations ##
MockHttpServer is single threaded (in this version) and is meant to be part of simple unit testing scenarios.  
It is not designed to stress test HTTP client code.