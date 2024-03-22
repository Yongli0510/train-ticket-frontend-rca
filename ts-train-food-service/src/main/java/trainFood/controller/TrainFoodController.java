package trainFood.controller;

import edu.fudan.common.util.Response;
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
import trainFood.service.TrainFoodService;

import javax.annotation.PostConstruct;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/trainfoodservice")
public class TrainFoodController {

    @Autowired
    TrainFoodService trainFoodService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainFoodController.class);

    private Counter getAllTrainFood_error;
    private Counter getTrainFoodOfTrip_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-train-food-service");
        meterRegistry.config().commonTags(tags);
        getAllTrainFood_error = Counter.builder("ts.train-food.getAllTrainFood.error").register(meterRegistry);
        getTrainFoodOfTrip_error = Counter.builder("ts.train-food.getTrainFoodOfTrip.error").register(meterRegistry);
    }

    @GetMapping(path = "/trainfoods/welcome")
    public String home() {
        return "Welcome to [ Train Food Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/trainfoods")
    @Timed(value = "ts.train-food.getAllTrainFood")
    public HttpEntity getAllTrainFood(@RequestHeader HttpHeaders headers) {
        TrainFoodController.LOGGER.info("[Food Map Service][Get All TrainFoods]");
        Response<?> response = trainFoodService.listTrainFood(headers);
        if (response.getStatus() != 1)
            getAllTrainFood_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/trainfoods/{tripId}")
    @Timed(value = "ts.train-food.getTrainFoodOfTrip")
    public HttpEntity getTrainFoodOfTrip(@PathVariable String tripId, @RequestHeader HttpHeaders headers) {
        TrainFoodController.LOGGER.info("[Food Map Service][Get TrainFoods By TripId]");
        Response<?> response = trainFoodService.listTrainFoodByTripId(tripId, headers);
        if (response.getStatus() != 1)
            getTrainFoodOfTrip_error.increment();
        return ok(response);
    }
}
