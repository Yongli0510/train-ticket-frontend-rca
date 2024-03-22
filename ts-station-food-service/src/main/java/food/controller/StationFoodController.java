package food.controller;

import edu.fudan.common.util.Response;
import food.service.StationFoodService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/stationfoodservice")
public class StationFoodController {

    @Autowired
    StationFoodService stationFoodService;

    private static final Logger LOGGER = LoggerFactory.getLogger(StationFoodController.class);

    private Counter getAllFoodStores_error;
    private Counter getFoodStoresOfStation_error;
    private Counter getFoodStoresByStationNames_error;
    private Counter getFoodListByStationFoodStoreId_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-station-food-service");
        meterRegistry.config().commonTags(tags);
        getAllFoodStores_error = Counter.builder("ts.station-food.getAllFoodStores.error").register(meterRegistry);
        getFoodStoresOfStation_error = Counter.builder("ts.station-food.getFoodStoresOfStation.error").register(meterRegistry);
        getFoodStoresByStationNames_error = Counter.builder("ts.station-food.getFoodStoresByStationNames.error").register(meterRegistry);
        getFoodListByStationFoodStoreId_error = Counter.builder("ts.station-food.getFoodListByStationFoodStoreId.error").register(meterRegistry);
    }

    @GetMapping(path = "/stationfoodstores/welcome")
    public String home() {
        return "Welcome to [ Food store Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/stationfoodstores")
    @Timed(value = "ts.station-food.getAllFoodStores")
    public HttpEntity getAllFoodStores(@RequestHeader HttpHeaders headers) {
        StationFoodController.LOGGER.info("[Food Map Service][Get All FoodStores]");
        Response<?> response = stationFoodService.listFoodStores(headers);
        if (response.getStatus() != 1)
            getAllFoodStores_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/stationfoodstores/{stationId}")
    @Timed(value = "ts.station-food.getFoodStoresOfStation")
    public HttpEntity getFoodStoresOfStation(@PathVariable String stationName, @RequestHeader HttpHeaders headers) {
        StationFoodController.LOGGER.info("[Food Map Service][Get FoodStores By StationName]");
        Response<?> response = stationFoodService.listFoodStoresByStationName(stationName, headers);
        if (response.getStatus() != 1)
            getFoodStoresOfStation_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/stationfoodstores")
    @Timed(value = "ts.station-food.getFoodStoresByStationNames")
    public HttpEntity getFoodStoresByStationNames(@RequestBody List<String> stationNameList) {
        StationFoodController.LOGGER.info("[Food Map Service][Get FoodStores By StationNames]");
        Response<?> response = stationFoodService.getFoodStoresByStationNames(stationNameList);
        if (response.getStatus() != 1)
            getFoodStoresByStationNames_error.increment();
        return ok(response);
    }

    @GetMapping("/stationfoodstores/bystoreid/{stationFoodStoreId}")
    @Timed(value = "ts.station-food.getFoodListByStationFoodStoreId")
    public HttpEntity getFoodListByStationFoodStoreId(@PathVariable String stationFoodStoreId, @RequestHeader HttpHeaders headers) {
        StationFoodController.LOGGER.info("[Food Map Service][Get Foodlist By stationFoodStoreId]");
        Response<?> response = stationFoodService.getStaionFoodStoreById(stationFoodStoreId);
        if (response.getStatus() != 1)
            getFoodListByStationFoodStoreId_error.increment();
        return ok(response);
    }
}
