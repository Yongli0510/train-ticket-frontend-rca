package rebook.controller;

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
import rebook.entity.RebookInfo;
import rebook.service.RebookService;

import javax.annotation.PostConstruct;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/rebookservice")
public class RebookController {

    @Autowired
    RebookService service;

    private static final Logger LOGGER = LoggerFactory.getLogger(RebookController.class);

    private Counter payDifference_error;
    private Counter rebook_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-rebook-service");
        meterRegistry.config().commonTags(tags);
        payDifference_error = Counter.builder("ts.rebook.payDifference.error").register(meterRegistry);
        rebook_error = Counter.builder("ts.rebook.rebook.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ Rebook Service ] !";
    }

    @PostMapping(value = "/rebook/difference")
    @Timed(value = "ts.rebook.payDifference")
    public HttpEntity payDifference(@RequestBody RebookInfo info,
                                    @RequestHeader HttpHeaders headers) {
        RebookController.LOGGER.info("[payDifference][Pay difference][OrderId: {}]", info.getOrderId());
        Response<?> response = service.payDifference(info, headers);
        if (response.getStatus() != 1)
            payDifference_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/rebook")
    @Timed(value = "ts.rebook.rebook")
    public HttpEntity rebook(@RequestBody RebookInfo info, @RequestHeader HttpHeaders headers) {
        RebookController.LOGGER.info("[rebook][Rebook][OrderId: {}, Old Trip Id: {}, New Trip Id: {}, Date: {}, Seat Type: {}]", info.getOrderId(), info.getOldTripId(), info.getTripId(), info.getDate(), info.getSeatType());
        Response<?> response = service.rebook(info, headers);
        if (response.getStatus() != 1)
            rebook_error.increment();
        return ok(response);
    }
}
