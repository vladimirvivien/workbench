package v.vivien.concurrency;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple tcp server using a Thread Pool via ThreadPoolExecutor.
 * @author vladimir
 */
public class EchoServerWithExecutor {
    private final static int SVR_PORT = 2345;
    
    public static class Server {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        SocketAddress port;
        ServerSocketChannel server;
        volatile boolean started;

        public Server(){
            port = new InetSocketAddress(SVR_PORT);
        }
        
        public void start() throws IOException{
            server = ServerSocketChannel.open();
            server.bind(port);
            started = true;
            System.out.println ("Server started, listening on port " + port.toString());
            while (started){
                final SocketChannel client = server.accept();
                Runnable clientHandler = new Runnable(){
                    @Override
                    public void run() {
                        try {
                            String rsp = "Connected to client " + client.getRemoteAddress().toString();
                            client.write(ByteBuffer.wrap(rsp.getBytes("UTF-8")));
                            
                            // do echo
                            ByteBuffer buff = ByteBuffer.allocate(1024);                            
                            while(true){
                                buff.clear();
                                
                                int byteCount = client.read(buff);
                                byte[] bytesRead = new byte[byteCount];
                                buff.flip();
                                buff.get(bytesRead,0,byteCount);
                               
                                String msg = new String(bytesRead);
                                System.out.println ("Rcvd " + msg);
                                if(msg.equals("quit")){
                                    Server.this.stop();
                                }
                                buff.flip();
                                while(buff.hasRemaining()){
                                    client.write(buff);
                                }
                            }
                            
                        } catch (IOException ex) {
                            throw new RuntimeException (ex);
                        }
                    }
                };
                
                pool.execute(clientHandler);
                
            }
        }
        
        public void stop() throws IOException {
            started = false;
            server.close();
            pool.shutdown();
        }
    }
    
    public static void main(String[] args) throws Exception{
        Server svr = new Server();
        svr.start();
    }
}
