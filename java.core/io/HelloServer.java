import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class HelloServer {
    private static final int PORT = 2345;
    public static void main (String[] args) throws Exception {
	SocketAddress port = new InetSocketAddress(PORT);
	ServerSocketChannel server = ServerSocketChannel.open();
	server.bind(port);
	System.out.print ("Server ready = " + server.isOpen() + "... ");
	System.out.println ("listening on port " + PORT);
	
	while(true){
	    SocketChannel clientChannel = server.accept();
	    String rsp = "Hello " + clientChannel.socket().getInetAddress();
	    ByteBuffer rspBuffer = ByteBuffer.wrap(rsp.getBytes("UTF-8"));
	    while(rspBuffer.hasRemaining()){
	        clientChannel.write(rspBuffer);    
	    }
	}

    }
}
