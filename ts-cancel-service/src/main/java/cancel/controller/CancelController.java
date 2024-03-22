package cancel.controller;

import cancel.service.CancelService;
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

import javax.annotation.PostConstruct;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/cancelservice")
public class CancelController {

    @Autowired
    CancelService cancelService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelController.class);

    private Counter calculate_error;
    private Counter cancelTicket_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-cancel-service");
        meterRegistry.config().commonTags(tags);
        calculate_error = Counter.builder("ts.cancel.calculate.error").register(meterRegistry);
        cancelTicket_error = Counter.builder("ts.cancel.cancelTicket.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Cancel Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/cancel/refound/{orderId}")
    @Timed(value = "ts.cancel.calculate")
    public HttpEntity calculate(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        CancelController.LOGGER.info("[calculate][Calculate Cancel Refund][OrderId: {}]", orderId);
        Response<?> response = cancelService.calculateRefund(orderId, headers);
        if (response.getStatus() != 1)
            calculate_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/cancel/{orderId}/{loginId}")
    @Timed(value = "ts.cancel.cancelTicket")
    public HttpEntity cancelTicket(@PathVariable String orderId, @PathVariable String loginId,
                                   @RequestHeader HttpHeaders headers) {

        CancelController.LOGGER.info("[cancelTicket][Cancel Ticket][info: {}]", orderId);
        try {
            CancelController.LOGGER.info("[cancelTicket][Cancel Ticket, Verify Success]");
            Response<?> response = cancelService.cancelOrder(orderId, loginId, headers);
            if (response.getStatus() != 1)
                cancelTicket_error.increment();
            return ok(response);
        } catch (Exception e) {
            CancelController.LOGGER.error(e.getMessage());
            cancelTicket_error.increment();
            return ok(new Response<>(1, "error", null));
        }
    }
}
