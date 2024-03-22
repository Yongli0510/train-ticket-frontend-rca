package fdse.microservice.controller;

import edu.fudan.common.entity.Travel;
import edu.fudan.common.util.Response;
import fdse.microservice.service.BasicService;
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

/**
 * @author Chenjie
 * @date 2017/6/6.
 */
@RestController
@RequestMapping("/api/v1/basicservice")

public class BasicController {

    @Autowired
    BasicService service;

    private static final Logger logger = LoggerFactory.getLogger(BasicController.class);

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter queryForTravel_error;
    private Counter queryForTravels_error;
    private Counter queryForStationId_error;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-basic-service");
        meterRegistry.config().commonTags(tags);
        queryForTravel_error = Counter.builder("ts.basic.queryForTravel.error").register(meterRegistry);
        queryForTravels_error = Counter.builder("ts.basic.queryForTravels.error").register(meterRegistry);
        queryForStationId_error = Counter.builder("ts.basic.queryForStationId.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Basic Service ] !";
    }

    @PostMapping(value = "/basic/travel")
    @Timed(value = "ts.basic.queryForTravel")
    public HttpEntity queryForTravel(@RequestBody Travel info, @RequestHeader HttpHeaders headers) {
        // TravelResult
        logger.info("[queryForTravel][Query for travel][Travel: {}]", info.toString());
        Response<?> response = service.queryForTravel(info, headers);
        if (response.getStatus() != 1)
            queryForTravel_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/basic/travels")
    @Timed(value = "ts.basic.queryForTravels")
    public HttpEntity queryForTravels(@RequestBody List<Travel> infos, @RequestHeader HttpHeaders headers) {
        // TravelResult
        logger.info("[queryForTravels][Query for travels][Travels: {}]", infos);
        Response<?> response = service.queryForTravels(infos, headers);
        if (response.getStatus() != 1)
            queryForTravels_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/basic/{stationName}")
    @Timed(value = "ts.basic.queryForStationId")
    public HttpEntity queryForStationId(@PathVariable String stationName, @RequestHeader HttpHeaders headers) {
        // String id
        logger.info("[queryForStationId][Query for stationId by stationName][stationName: {}]", stationName);
        Response<?> response = service.queryForStationId(stationName, headers);
        if (response.getStatus() != 1)
            queryForStationId_error.increment();
        return ok(response);
    }
}
