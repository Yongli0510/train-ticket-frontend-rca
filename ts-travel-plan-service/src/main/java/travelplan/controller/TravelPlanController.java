package travelplan.controller;

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
import edu.fudan.common.entity.TripInfo;
import travelplan.entity.TransferTravelInfo;
import travelplan.service.TravelPlanService;

import javax.annotation.PostConstruct;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("api/v1/travelplanservice")
public class TravelPlanController {

    @Autowired
    TravelPlanService travelPlanService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TravelPlanController.class);

    @Autowired
    private MeterRegistry meterRegistry;
    private Counter getByCheapest_error;
    private Counter getByQuickest_error;
    private Counter getByMinStation_error;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-travel-plan-service");
        meterRegistry.config().commonTags(tags);
        getByCheapest_error = Counter.builder("ts.travel-plan.getByCheapest.error").register(meterRegistry);
        getByQuickest_error = Counter.builder("ts.travel-plan.getByQuickest.error").register(meterRegistry);
        getByMinStation_error = Counter.builder("ts.travel-plan.getByMinStation.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ TravelPlan Service ] !";
    }

    @PostMapping(value = "/travelPlan/transferResult")
    @Timed(value = "ts.travel-plan.getTransferResult")
    public HttpEntity getTransferResult(@RequestBody TransferTravelInfo info, @RequestHeader HttpHeaders headers) {
        TravelPlanController.LOGGER.info("[getTransferSearch][Search Transit][start: {},end: {}]", info.getStartStation(), info.getEndStation());
        return ok(travelPlanService.getTransferSearch(info, headers));
    }

    @PostMapping(value = "/travelPlan/cheapest")
    @Timed(value = "ts.travel-plan.getByCheapest")
    public HttpEntity getByCheapest(@RequestBody TripInfo queryInfo, @RequestHeader HttpHeaders headers) {
        TravelPlanController.LOGGER.info("[getCheapest][Search Cheapest][start: {},end: {},time: {}]", queryInfo.getStartPlace(), queryInfo.getEndPlace(), queryInfo.getDepartureTime());
        Response<?> response = travelPlanService.getCheapest(queryInfo, headers);
        if (response.getStatus() != 1)
            getByCheapest_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/travelPlan/quickest")
    @Timed(value = "ts.travel-plan.getByQuickest")
    public HttpEntity getByQuickest(@RequestBody TripInfo queryInfo, @RequestHeader HttpHeaders headers) {
        TravelPlanController.LOGGER.info("[getQuickest][Search Quickest][start: {},end: {},time: {}]", queryInfo.getStartPlace(), queryInfo.getEndPlace(), queryInfo.getDepartureTime());
        Response<?> response = travelPlanService.getQuickest(queryInfo, headers);
        if (response.getStatus() != 1)
            getByQuickest_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/travelPlan/minStation")
    @Timed(value = "ts.travel-plan.getByMinStation")
    public HttpEntity getByMinStation(@RequestBody TripInfo queryInfo, @RequestHeader HttpHeaders headers) {
        TravelPlanController.LOGGER.info("[getMinStation][Search Min Station][start: {},end: {},time: {}]", queryInfo.getStartPlace(), queryInfo.getEndPlace(), queryInfo.getDepartureTime());
        Response<?> response = travelPlanService.getMinStation(queryInfo, headers);
        if (response.getStatus() != 1)
            getByMinStation_error.increment();
        return ok(response);
    }
}
