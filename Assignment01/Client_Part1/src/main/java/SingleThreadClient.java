import Model.EventGenerator;
import Model.LiftRideEvent;
import Model.PostProcessor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadClient{

    private static String urlBase;
    private static AtomicInteger successReq;
    private static AtomicInteger failReq;
    private static BlockingQueue<LiftRideEvent> events;
    private static int totalReq = 1000;

    public static void main(String[] args) throws InterruptedException {
        urlBase = "http://localhost:8081/Server_war_exploded/";
        successReq = new AtomicInteger(0);
        failReq = new AtomicInteger(0);
        events = new LinkedBlockingQueue<>();

        System.out.println("*********************************************************");
        System.out.println("Processing Begins");
        System.out.println("*********************************************************");
        long start = System.currentTimeMillis();
        EventGenerator eventGenerator = new EventGenerator(events, totalReq);
        Thread generatorThread = new Thread(eventGenerator);
        generatorThread.start();

        CountDownLatch latch = new CountDownLatch(1);
        PostProcessor postProcessor = new PostProcessor(urlBase, true, successReq, failReq, totalReq, events, latch);
        Thread thread = new Thread(postProcessor);
        thread.start();
        latch.await();

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
