import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class URLGrab {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
	    System.out.println ("Usage: java URLGrab <URL> [<file name>]");
	    System.exit(-1);
    	}

	URL url = new URL(args[0]);
	SocketChannel socket = SocketChannel.open(new InetSocketAddress(url.getHost(), url.getPort()));

	StringBuilder headers = new StringBuilder("GET " + url.getFile() + " HTTP/1.1 \r\n");
	headers.append("User-Agent: HTTPGrabber\r\n")
		.append("Accept: text/*\r\n")
		.append("Connection: close\r\n")
		.append("Host: ").append(url.getHost()).append("\r\n")
		.append("\r\n");

	ByteBuffer headerBuff = ByteBuffer.wrap(headers.toString().getBytes("US-ASCII")); 
	ByteBuffer bodyBuff = ByteBuffer.allocate(8192);
	
	try{
	    System.out.println ("Socket ready = " + socket.isOpen());
	    socket.write(headerBuff);
	    while(socket.read(bodyBuff) != -1){
	        bodyBuff.flip();
		System.out.print(new String(bodyBuff.array()));
		bodyBuff.clear();
	    }
	    
	}finally{
	    socket.close();
	}
    }
}
