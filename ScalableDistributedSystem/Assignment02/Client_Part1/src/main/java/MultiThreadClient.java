import Model.EventGenerator;
import Model.LiftRideEvent;
import Model.PostProcessor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadClient {
    private static String urlBase;
    private static AtomicInteger successReq;
    private static AtomicInteger failReq;
    private static BlockingQueue<LiftRideEvent> events;
    private static final int numOfThread = 32;
    private static final int firstNumWork = 1000;
    private static final int totalReq = 200000;

    public static void main(String[] args) throws InterruptedException {
        urlBase = "http://localhost:8081/Server_war_exploded/";
        successReq = new AtomicInteger(0);
        failReq = new AtomicInteger(0);
        events = new LinkedBlockingQueue<>();

        System.out.println("*********************************************************");
        System.out.println("Processing Begins");
        System.out.println("*********************************************************");

        CountDownLatch latch1 = new CountDownLatch(numOfThread);
        CountDownLatch latch2 = new CountDownLatch(numOfThread);
        long start = System.currentTimeMillis();
        EventGenerator eventGenerator = new EventGenerator(events, totalReq);
        Thread generatorThread = new Thread(eventGenerator);
        generatorThread.start();

        for (int i = 0; i < numOfThread; i++) {
            PostProcessor postProcessor = new PostProcessor(urlBase, true, successReq, failReq, firstNumWork, events, latch1);
            Thread thread = new Thread(postProcessor);
            thread.start();
        }
        latch1.await();

        for (int j = 0; j < numOfThread; j++) {
            PostProcessor postProcessor = new PostProcessor(urlBase, false, successReq, failReq, firstNumWork, events, latch2);
            Thread thread = new Thread(postProcessor);
            thread.start();
        }
        latch2.await();

        long end = System.currentTimeMillis();
        long wallTime = end - start;

        System.out.println("*********************************************************");
        System.out.println("Processing Ends");
        System.out.println("*********************************************************");
        System.out.println("Number of successful requests :" + successReq.get());
        System.out.println("Number of failed requests :" + failReq.get());
        System.out.println("Total wall time: " + wallTime);
        System.out.println( "Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
    }
}
