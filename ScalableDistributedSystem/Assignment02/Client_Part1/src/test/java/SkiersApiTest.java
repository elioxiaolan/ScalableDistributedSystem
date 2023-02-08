import io.swagger.client.*;
import io.swagger.client.model.*;
import io.swagger.client.api.SkiersApi;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class SkiersApiTest {
    ApiClient apiClient = new ApiClient();
    SkiersApi skiersApi = new SkiersApi(apiClient);

    public SkiersApiTest() {
        skiersApi.getApiClient().setBasePath("http://localhost:8081/Server_war_exploded");
    }

    @Test
    public void writeNewLiftRideWithHttpInfoTest() throws Exception {
        LiftRide liftRide = new LiftRide();
        liftRide.setTime(123);
        liftRide.setLiftID(456);
        Integer resortId = 123;
        String seasonId = "2022";
        String dayId = "1011";
        Integer skierId = 456;
        try {
            skiersApi.writeNewLiftRideWithHttpInfo(liftRide, resortId, seasonId, dayId, skierId);
        } catch (ApiException e) {
            System.err.println("Exception when calling SkiersApi#writeNewLiftRideWithHttpInfo");
            e.printStackTrace();
        }
    }
}