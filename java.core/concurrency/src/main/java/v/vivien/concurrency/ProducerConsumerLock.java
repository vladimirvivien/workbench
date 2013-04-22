package v.vivien.concurrency;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vladimir.vivien
 */
public class ProducerConsumerLock {
    public static class MessageQueue {
        String[] queue;
        int slotCount, putCount, takeCount;
        final ReentrantLock lock = new ReentrantLock();
        final Condition putIsPossible = lock.newCondition();
        final Condition takeIsPossible = lock.newCondition();
        
        public MessageQueue() {
            queue = new String[10];
        }
        public MessageQueue(int size){
            queue = new String[(size < 1) ? 1 : size];
        }
        
        /**
         * put message into buffer.
         * Only put message in queue when there is an available slot.
         * @param msg 
         */
        public void put(String msg) throws InterruptedException{
            lock.lock();
            try{
                printPutStats(msg);
                while ( queueIsFull() ){
                    System.out.println ("\tQueue Full, waiting to put");
                    putIsPossible.await();
                }
                if(putCount >= queue.length) putCount = 0;
                queue[putCount++] = msg;
                slotCount++;
                takeIsPossible.signal(); // signal waiting waiting thread for take operation.
            }finally{
                lock.unlock();
            }
        }
        
        public String take() throws InterruptedException {
            lock.lock();
            try{
                printTakeStats();
                while( queueIsEmpty() ){
                    System.out.println ("\tQueue empty, waiting to take");
                    takeIsPossible.await();
                }
                if(takeCount >= queue.length) takeCount = 0;
                String msg = queue[takeCount++];
                slotCount--;
                putIsPossible.signal(); // signal waiting thread that put is possible.
                return msg;
            }finally{
                lock.unlock();
            }
        }
        private void printPutStats(String msg){
            System.out.println ("\tRcvd msg (" + msg + ")");
            System.out.println ("\tThreads waiting for lock " + lock.getQueueLength() );
            System.out.println ("\tThreads waiting to put " + lock.getWaitQueueLength(putIsPossible));
        }
        private void printTakeStats(){
            System.out.println ("\tThreads waiting for lock " + lock.getQueueLength() );
            System.out.println ("\tThreads waiting to take " + lock.getWaitQueueLength(takeIsPossible));
        }
        
        private boolean queueIsFull(){
            return slotCount == queue.length;
        }
        private boolean queueIsEmpty() {
            return slotCount == 0;
        }
    }
    
    private static int QUEUE_SIZE = 5;
    
    public static void main (String[] args) throws Exception{
        final MessageQueue queue = new MessageQueue(QUEUE_SIZE);
        
        // create producer threads
        for(int i = 0; i < 1; i++){
            new Thread(new Runnable(){
                @Override
                public void run() {
                    Random rnd = new Random();
                    try {
                        for(int j = 0; j < 300; j++){
                            String msg = new Date().toString();
                            System.out.println ("PRODUCER.put("+msg+")");
                            queue.put(msg);
                            Thread.sleep(rnd.nextInt(2000));
                        }
                    } catch (InterruptedException ex) {}
                }
            },"PRODUCER-"+i).start();
        }
        
        // create consumer threads
        for(int i = 0; i < 2; i++){
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        for(int j = 0; j < 50
                                ; j++){
                            System.out.println ("CONSUMER taking Message");
                            String msg = queue.take();
                            System.out.println ("\tCONSUMER.took(" + msg +")");
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException ex) {}
                }
            },"CONSUMER-"+i).start();
        }
    }
}
