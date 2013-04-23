import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class NTPClient {
    private static long TIME_EPOCH_ADJUSTMENT = 2208988800L;
    private static int _8K = 8192;

    public static void main(String[] args) throws Exception {
	DatagramChannel clientChannel = null;
	try{
	    clientChannel = DatagramChannel.open();
	    DatagramSocket clientSocket = clientChannel.socket();
	    clientSocket.setSoTimeout(5000);
	    clientSocket.bind(new InetSocketAddress(0));
	    
	    SocketAddress serverAddr = new InetSocketAddress("time.nist.gov",37);
	    ByteBuffer req = ByteBuffer.allocate(_8K);
	    req.order(ByteOrder.BIG_ENDIAN);
	    ByteBuffer rsp = ByteBuffer.allocate(_8K);
	    rsp.order(ByteOrder.BIG_ENDIAN);

	    // send request (arbitrary byte value to server)
	    req.put((byte)65);
            req.flip();
	    clientChannel.send(req, serverAddr);

	    // receive response (as 4-byte unsigned big-endien value)
	    rsp.put((byte)0).put((byte)0).put((byte)0).put((byte)0);
	    clientChannel.receive(rsp);
	    rsp.flip();
	    long serverTime = rsp.getLong();

	    // adjust response for Java clock epoch (1970) and NTP (1900) in seconds.
	    long javaTimeInSec = serverTime -  TIME_EPOCH_ADJUSTMENT;

	    System.out.println (new java.util.Date(javaTimeInSec * 1000));

	}finally{
	    if(clientChannel != null) clientChannel.close();
	}
    }
}
