package foodsearch.controller;

import edu.fudan.common.util.JsonUtils;
import edu.fudan.common.util.Response;
import foodsearch.entity.*;
import foodsearch.mq.RabbitSend;
import foodsearch.service.FoodService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/foodservice")
public class FoodController {

    @Autowired
    FoodService foodService;

    @Autowired
    RabbitSend sender;

    private static final Logger LOGGER = LoggerFactory.getLogger(FoodController.class);

    private Counter findAllFoodOrder_error;
    private Counter createFoodOrder_error;
    private Counter createFoodBatches_error;
    private Counter updateFoodOrder_error;
    private Counter deleteFoodOrder_error;
    private Counter findFoodOrderByOrderId_error;
    private Counter getAllFood_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-food-service");
        meterRegistry.config().commonTags(tags);
        findAllFoodOrder_error = Counter.builder("ts.food.findAllFoodOrder.error").register(meterRegistry);
        createFoodOrder_error = Counter.builder("ts.food.createFoodOrder.error").register(meterRegistry);
        createFoodBatches_error = Counter.builder("ts.food.createFoodBatches.error").register(meterRegistry);
        updateFoodOrder_error = Counter.builder("ts.food.updateFoodOrder.error").register(meterRegistry);
        deleteFoodOrder_error = Counter.builder("ts.food.deleteFoodOrder.error").register(meterRegistry);
        findFoodOrderByOrderId_error = Counter.builder("ts.food.findFoodOrderByOrderId.error").register(meterRegistry);
        getAllFood_error = Counter.builder("ts.food.getAllFood.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ Food Service ] !";
    }

    @GetMapping(path = "/test_send_delivery")
    public boolean test_send_delivery() {
        Delivery delivery = new Delivery();
        delivery.setFoodName("HotPot");
        delivery.setOrderId(UUID.randomUUID());
        delivery.setStationName("Shang Hai");
        delivery.setStoreName("MiaoTing Instant-Boiled Mutton");

        String deliveryJson = JsonUtils.object2Json(delivery);
        sender.send(deliveryJson);
        return true;
    }

    @GetMapping(path = "/orders")
    @Timed(value = "ts.food.findAllFoodOrder")
    public HttpEntity findAllFoodOrder(@RequestHeader HttpHeaders headers) {
        FoodController.LOGGER.info("[Food Service]Try to Find all FoodOrder!");
        Response<?> response = foodService.findAllFoodOrder(headers);
        if (response.getStatus() != 1)
            findAllFoodOrder_error.increment();
        return ok(response);
    }

    @PostMapping(path = "/orders")
    @Timed(value = "ts.food.createFoodOrder")
    public HttpEntity createFoodOrder(@RequestBody FoodOrder addFoodOrder, @RequestHeader HttpHeaders headers) {
        FoodController.LOGGER.info("[createFoodOrder][Try to Create a FoodOrder!]");
        Response<?> response = foodService.createFoodOrder(addFoodOrder, headers);
        if (response.getStatus() != 1)
            createFoodOrder_error.increment();
        return ok(response);
    }

    @PostMapping(path = "/createOrderBatch")
    @Timed(value = "ts.food.createFoodBatches")
    public HttpEntity createFoodBatches(@RequestBody List<FoodOrder> foodOrderList, @RequestHeader HttpHeaders headers) {
        FoodController.LOGGER.info("[createFoodBatches][Try to Create Food Batches!]");
        Response<?> response = foodService.createFoodOrdersInBatch(foodOrderList, headers);
        if (response.getStatus() != 1)
            createFoodBatches_error.increment();
        return ok(response);
    }


    @PutMapping(path = "/orders")
    @Timed(value = "ts.food.updateFoodOrder")
    public HttpEntity updateFoodOrder(@RequestBody FoodOrder updateFoodOrder, @RequestHeader HttpHeaders headers) {
        FoodController.LOGGER.info("[updateFoodOrder][Try to Update a FoodOrder!]");
        Response<?> response = foodService.updateFoodOrder(updateFoodOrder, headers);
        if (response.getStatus() != 1)
            updateFoodOrder_error.increment();
        return ok(response);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping(path = "/orders/{orderId}")
    @Timed(value = "ts.food.deleteFoodOrder")
    public HttpEntity deleteFoodOrder(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        FoodController.LOGGER.info("[deleteFoodOrder][Try to Cancel a FoodOrder!]");
        Response<?> response = foodService.deleteFoodOrder(orderId, headers);
        if (response.getStatus() != 1)
            deleteFoodOrder_error.increment();
        return ok(response);
    }

    @GetMapping(path = "/orders/{orderId}")
    @Timed(value = "ts.food.findFoodOrderByOrderId")
    public HttpEntity findFoodOrderByOrderId(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        FoodController.LOGGER.info("[findFoodOrderByOrderId][Try to Find FoodOrder By orderId!][orderId: {}]", orderId);
        Response<?> response = foodService.findByOrderId(orderId, headers);
        if (response.getStatus() != 1)
            findFoodOrderByOrderId_error.increment();
        return ok(response);
    }

    // This relies on a lot of other services, not completely modified
    @GetMapping(path = "/foods/{date}/{startStation}/{endStation}/{tripId}")
    @Timed(value = "ts.food.getAllFood")
    public HttpEntity getAllFood(@PathVariable String date, @PathVariable String startStation,
                                 @PathVariable String endStation, @PathVariable String tripId,
                                 @RequestHeader HttpHeaders headers) {
        FoodController.LOGGER.info("[getAllFood][Get Food Request!]");
        Response<?> response = foodService.getAllFood(date, startStation, endStation, tripId, headers);
        if (response.getStatus() != 1)
            getAllFood_error.increment();
        return ok(response);
    }

}
