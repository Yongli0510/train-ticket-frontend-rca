package travel2.controller;

import edu.fudan.common.entity.TripResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travel2.service.TravelService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/travel2service")
public class Travel2Controller {
    @Autowired
    private TravelService service;

    private static final Logger LOGGER = LoggerFactory.getLogger(Travel2Controller.class);

    @Autowired
    private MeterRegistry meterRegistry;

    // ErrorCounter Java代码
    private Counter getTrainTypeByTripId_error;
    private Counter getRouteByTripId_error;
    private Counter getTripsByRouteId_error;
    private Counter createTrip_error;
    private Counter retrieve_error;
    private Counter updateTrip_error;
    private Counter deleteTrip_error;
    private Counter queryInfo_error;
    private Counter getTripAllDetailInfo_error;
    private Counter queryAll_error;
    private Counter adminQueryAll_error;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-travel2-service");
        meterRegistry.config().commonTags(tags);
        getTrainTypeByTripId_error = Counter.builder("ts.travel2.getTrainTypeByTripId.error").register(meterRegistry);
        getRouteByTripId_error = Counter.builder("ts.travel2.getRouteByTripId.error").register(meterRegistry);
        getTripsByRouteId_error = Counter.builder("ts.travel2.getTripsByRouteId.error").register(meterRegistry);
        createTrip_error = Counter.builder("ts.travel2.createTrip.error").register(meterRegistry);
        retrieve_error = Counter.builder("ts.travel2.retrieve.error").register(meterRegistry);
        updateTrip_error = Counter.builder("ts.travel2.updateTrip.error").register(meterRegistry);
        deleteTrip_error = Counter.builder("ts.travel2.deleteTrip.error").register(meterRegistry);
        queryInfo_error = Counter.builder("ts.travel2.queryInfo.error").register(meterRegistry);
        getTripAllDetailInfo_error = Counter.builder("ts.travel2.getTripAllDetailInfo.error").register(meterRegistry);
        queryAll_error = Counter.builder("ts.travel2.queryAll.error").register(meterRegistry);
        adminQueryAll_error = Counter.builder("ts.travel2.adminQueryAll.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Travle2 Service ] !";
    }

    @GetMapping(value = "/train_types/{tripId}")
    @Timed(value = "ts.travel2.getTrainTypeByTripId")
    public HttpEntity getTrainTypeByTripId(@PathVariable String tripId,
                                           @RequestHeader HttpHeaders headers) {
        // TrainType
        Travel2Controller.LOGGER.info("[getTrainTypeByTripId][Get train by Trip id][TripId: {}]", tripId);
        Response<?> response = service.getTrainTypeByTripId(tripId, headers);
        if (response.getStatus() != 1)
            getTrainTypeByTripId_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/routes/{tripId}")
    @Timed(value = "ts.travel2.getRouteByTripId")
    public HttpEntity getRouteByTripId(@PathVariable String tripId,
                                       @RequestHeader HttpHeaders headers) {
        Travel2Controller.LOGGER.info("[getRouteByTripId][Get Route By Trip ID][TripId: {}]", tripId);
        //Route
        Response<?> response = service.getRouteByTripId(tripId, headers);
        if (response.getStatus() != 1)
            getRouteByTripId_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/trips/routes")
    @Timed(value = "ts.travel2.getTripsByRouteId")
    public HttpEntity getTripsByRouteId(@RequestBody ArrayList<String> routeIds,
                                        @RequestHeader HttpHeaders headers) {
        // ArrayList<ArrayList<Trip>>
        Travel2Controller.LOGGER.info("[getTripByRoute][Get trips by Route id][RouteIdNumber: {}]", routeIds.size());
        Response<?> response = service.getTripByRoute(routeIds, headers);
        if (response.getStatus() != 1)
            getTripsByRouteId_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/trips")
    @Timed(value = "ts.travel2.createTrip")
    public HttpEntity<?> createTrip(@RequestBody edu.fudan.common.entity.TravelInfo routeIds, @RequestHeader HttpHeaders headers) {
        // null
        Travel2Controller.LOGGER.info("[create][Create trip][TripId: {}]", routeIds.getTripId());
        Response<?> response = service.create(routeIds, headers);
        if (response.getStatus() != 1)
            createTrip_error.increment();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Return Trip only, no left ticket information
     *
     * @param tripId  trip id
     * @param headers headers
     * @return HttpEntity
     */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/trips/{tripId}")
    @Timed(value = "ts.travel2.retrieve")
    public HttpEntity retrieve(@PathVariable String tripId, @RequestHeader HttpHeaders headers) {
        // Trip
        Travel2Controller.LOGGER.info("[retrieve][Retrieve trip][TripId: {}]", tripId);
        Response<?> response = service.retrieve(tripId, headers);
        if (response.getStatus() != 1)
            retrieve_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(value = "/trips")
    @Timed(value = "ts.travel2.updateTrip")
    public HttpEntity updateTrip(@RequestBody edu.fudan.common.entity.TravelInfo info, @RequestHeader HttpHeaders headers) {
        // Trip
        Travel2Controller.LOGGER.info("[update][Update trip][TripId: {}]", info.getTripId());
        Response<?> response = service.update(info, headers);
        if (response.getStatus() != 1)
            updateTrip_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/trips/{tripId}")
    @Timed(value = "ts.travel2.deleteTrip")
    public HttpEntity deleteTrip(@PathVariable String tripId, @RequestHeader HttpHeaders headers) {
        // string
        Travel2Controller.LOGGER.info("[delete][Delete trip][TripId: {}]", tripId);
        Response<?> response = service.delete(tripId, headers);
        if (response.getStatus() != 1)
            deleteTrip_error.increment();
        return ok(response);
    }

    /**
     * Return Trip and the remaining tickets
     *
     * @param info    trip info
     * @param headers headers
     * @return HttpEntity
     */
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/trips/left")
    @Timed(value = "ts.travel2.queryInfo")
    public HttpEntity queryInfo(@RequestBody edu.fudan.common.entity.TripInfo info, @RequestHeader HttpHeaders headers) {
        if (info.getStartPlace() == null || info.getStartPlace().length() == 0 ||
                info.getEndPlace() == null || info.getEndPlace().length() == 0 ||
                info.getDepartureTime() == null) {
            Travel2Controller.LOGGER.info("[query][Travel Query Fail][Something null]");
            queryInfo_error.increment();
            ArrayList<TripResponse> errorList = new ArrayList<>();
            return ok(errorList);
        }
        Travel2Controller.LOGGER.info("[query][Query TripResponse]");
        return ok(service.queryByBatch(info, headers));
    }

    /**
     * Return a Trip and the remaining tickets
     *
     * @param gtdi    trip all datail info
     * @param headers headers
     * @return HttpEntity
     */
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/trip_detail")
    @Timed(value = "ts.travel2.getTripAllDetailInfo")
    public HttpEntity getTripAllDetailInfo(@RequestBody edu.fudan.common.entity.TripAllDetailInfo gtdi, @RequestHeader HttpHeaders headers) {
        Travel2Controller.LOGGER.info("[getTripAllDetailInfo][Get trip detail][TripId: {}]", gtdi.getTripId());
        Response<?> response = service.getTripAllDetailInfo(gtdi, headers);
        if (response.getStatus() != 1)
            getTripAllDetailInfo_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/trips")
    @Timed(value = "ts.travel2.queryAll")
    public HttpEntity queryAll(@RequestHeader HttpHeaders headers) {
        // List<Trip>
        Travel2Controller.LOGGER.info("[queryAll][Query all trips]");
        Response<?> response = service.queryAll(headers);
        if (response.getStatus() != 1)
            queryAll_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/admin_trip")
    @Timed(value = "ts.travel2.adminQueryAll")
    public HttpEntity adminQueryAll(@RequestHeader HttpHeaders headers) {
        // ArrayList<AdminTrip>
        Travel2Controller.LOGGER.info("[adminQueryAll][Admin query all trips]");
        Response<?> response = service.adminQueryAll(headers);
        if (response.getStatus() != 1)
            adminQueryAll_error.increment();
        return ok(response);
    }
}
