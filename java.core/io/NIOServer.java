import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
* Sample to show how to create an non-blocking NIO server using the Selector API.
*/

public class NIOServer {
    private static byte[] DATA = new byte[255];
    static {
        for(int i = 0; i < DATA.length; i++) DATA[i] = (byte) i;
    }

    public static void main(String[] args) throws Exception {

	// configure server channel and its selector
	ServerSocketChannel server = ServerSocketChannel.open();
	server.configureBlocking(false);
	server.socket().bind(new InetSocketAddress(2345));
	System.out.println ("Server listeining on port 2345");
	Selector selector = Selector.open();
	server.register(selector, SelectionKey.OP_ACCEPT);

	// set server's selector loop
	while (true)  {
	    selector.select();
	    Set selectedKeys = selector.selectedKeys();

	    // loop through ready selected keys to handle
	    Iterator i = selectedKeys.iterator();
	    while(i.hasNext()){
		SelectionKey key = (SelectionKey) i.next();
		i.remove(); // avoid multiple handling in same loop

		// process selector (selected) key
		try{

		    // accepting client connection
		    if(key.isAcceptable()){
			SocketChannel client = server.accept();
			client.configureBlocking(false);
			
			// register a write-operation & data (to write) for client
			SelectionKey clientKey = client.register(selector, SelectionKey.OP_WRITE);
			clientKey.attach(ByteBuffer.wrap(DATA));
			System.out.println ("Connected to client ... ");
		    }

		    // writing response to client
		    if(key.isWritable()){
			SocketChannel client = (SocketChannel) key.channel();
			ByteBuffer response = (ByteBuffer) key.attachment();
			if(response.hasRemaining()) response.rewind();
			client.write(response);
		    }

		}catch(IOException ex){
		    key.cancel();
		    key.channel().close();
		}
	    } 
	}

    }
}
