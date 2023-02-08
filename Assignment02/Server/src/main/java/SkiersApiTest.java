import io.swagger.client.*;
import io.swagger.client.model.*;
import io.swagger.client.api.SkiersApi;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class SkiersApiTest {
    public static void main(String[] args) {
        try {
            ApiClient apiClient = new ApiClient();
            SkiersApi skiersApi = new SkiersApi(apiClient);
            skiersApi.getApiClient().setBasePath("http://localhost:8081/Server_war_exploded");
            LiftRide liftRide = new LiftRide();
            liftRide.setTime(123);
            liftRide.setLiftID(456);
            Integer resortId = 123;
            String seasonId = "2022";
            String dayId = "1011";
            Integer skierId = 456;
            ApiResponse<Void> response = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, resortId, seasonId, dayId, skierId);
        } catch (ApiException e) {
            System.err.println("Exception when calling SkiersApi#writeNewLiftRideWithHttpInfo");
            e.printStackTrace();
        }
    }
}
