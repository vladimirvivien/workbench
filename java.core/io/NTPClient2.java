import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class NTPClient2 {
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
	    clientChannel.receive(rsp);
	    rsp.flip();

	    long serverTime = 0;
	    byte byte1 = rsp.get();
	    byte byte2 = rsp.get();
            byte byte3 = rsp.get();
            byte byte4 = rsp.get();

	    // build 64-bit long value from the 4 bytes received from NTP server.
	    // The first 4-byte most sig values are filled with zero's
	    // the values from the server are stuffed in the last for bytes as shown below:  
	    // [0][0][0][0][byte1][byte2][byte3][byte4]
	    serverTime = (
		0L |
		((long)(byte1 & 0xFF) << 24) |
		((long)(byte2 & 0xFF) << 16) |
		((long)(byte3 & 0xFF) << 8) |
		((long)(byte4 & 0xFF))
	    );
		
		
	    // adjust response for Java clock epoch (1970) and NTP (1900) in seconds.
	    long javaTimeInSec = serverTime -  TIME_EPOCH_ADJUSTMENT;

	    System.out.println (new java.util.Date(javaTimeInSec * 1000));

	}finally{
	    if(clientChannel != null) clientChannel.close();
	}
    }
}
