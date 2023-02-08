package Model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class PostProcessor implements Runnable {
    private String urlBase;
    private boolean isFirstWork;
    private AtomicInteger successReq;
    private AtomicInteger failReq;
    private int totalReq;
    private BlockingQueue<LiftRideEvent> events;
    private CountDownLatch finishLatch;
    private List<String[]> records;

    public PostProcessor(String urlBase, boolean isFirstWork, AtomicInteger successReq, AtomicInteger failReq, int totalReq,
                         BlockingQueue<LiftRideEvent> events, CountDownLatch finishLatch, List<String[]> records) {
        this.urlBase = urlBase;
        this.isFirstWork = isFirstWork;
        this.successReq = successReq;
        this.failReq = failReq;
        this.totalReq = totalReq;
        this.events = events;
        this.finishLatch = finishLatch;
        this.records = records;
    }


    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        SkiersApi skiersApi = new SkiersApi(apiClient);
        skiersApi.getApiClient().setBasePath(this.urlBase);
        int successCount = 0;
        int failCount = 0;

        if (this.isFirstWork) {
            for (int i = 0; i < this.totalReq; i++) {
                LiftRideEvent curEvent = this.events.poll();
                if (postWithEvent(skiersApi, curEvent)) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
        } else {
            while (events.peek().getResortId() != 11) {
                LiftRideEvent curEvent = this.events.poll();
                if (postWithEvent(skiersApi, curEvent)) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
        }

        successReq.getAndAdd(successCount);
        failReq.getAndAdd(failCount);
        finishLatch.countDown();
    }

    public boolean postWithEvent(SkiersApi skiersApi, LiftRideEvent curEvent) {
        int retry = 0;

        while (retry < 5) {
            try {
                long start = System.currentTimeMillis();
                LiftRide liftRide = new LiftRide();
                liftRide.setLiftID(curEvent.getLiftId());
                liftRide.setTime(curEvent.getTime());
                ApiResponse<Void> res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, curEvent.getResortId(), curEvent.getSeasonId(), curEvent.getDayId(), curEvent.getSkierId());
                if (res.getStatusCode() == 201) {
                    long end = System.currentTimeMillis();
                    System.out.println(end - start);
                    String[] curRecord = new String[4];
                    curRecord[0] = String.valueOf(start);
                    curRecord[1] = "POST";
                    curRecord[2] = String.valueOf(end - start);
                    curRecord[3] = String.valueOf(res.getStatusCode());
                    this.records.add(curRecord);
                    return true;
                }
            } catch (ApiException e) {
                retry++;
                e.printStackTrace();
            }
        }

        return false;
    }
}
