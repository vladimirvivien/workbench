package v.vivien.concurrency;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Project Euler (http://projecteuler.net/problem1)
 * 
 * If we list all the natural numbers below 10 that are multiples of 3 or 5, we get 3, 5, 6 and 9. 
 * The sum of these multiples is 23.
 * Find the sum of all the multiples of 3 or 5 below 1000. 
 
 * @author vladimir.vivien
 */
public class PrimeSumWithLatch {
    private static final int N = 1000000;
    private static int[] VALUES = new int[N];
    private static final int SLICE_COUNT = 1;
    private static final int SLICE_SIZE    = N/SLICE_COUNT;
    private static final int THREAD_COUNT = SLICE_COUNT;
    
    private static ExecutorService threads = Executors.newFixedThreadPool(THREAD_COUNT);
    private static CountDownLatch primerSignal = new CountDownLatch(THREAD_COUNT);
    
    public static class Primer implements Runnable {
        int startPos;
        public Primer(int pos){
            this.startPos = pos;
        }
        @Override
        public void run() {
            for(int i = startPos; i < startPos + SLICE_SIZE; i++ ){
                if((i % 3) == 0) VALUES[i] = i;
                if((i % 5) == 0) VALUES[i] = i;
            }
            primerSignal.countDown();
        }
    }
    
    public static long counter (){
        long result = 0;
        for(int i = 0; i < N; i++){
            result = result + VALUES[i];
        }
        
        return result;
    }
    
    public static void main(String[] args) throws Exception {
        long zMillis = System.currentTimeMillis();
        // create Primers
        for(int i = 0; i < THREAD_COUNT; i++){
            threads.execute(new Primer(i * 100));
        }
        primerSignal.await();
        threads.shutdownNow();
        
        System.out.println ("The sum of all the multiples of 3 or 5 below 1000: " + counter());
        System.out.println ("Done in " + (System.currentTimeMillis() - zMillis) + " millis");
    }
}
