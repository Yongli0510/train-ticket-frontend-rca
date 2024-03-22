package execute.controller;

import edu.fudan.common.util.Response;
import execute.serivce.ExecuteService;
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
@RequestMapping("/api/v1/executeservice")
public class ExecuteControlller {

    @Autowired
    private ExecuteService executeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteControlller.class);

    private Counter executeTicket_error;
    private Counter collectTicket_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-execute-service");
        meterRegistry.config().commonTags(tags);
        executeTicket_error = Counter.builder("ts.execute.executeTicket.error").register(meterRegistry);
        collectTicket_error = Counter.builder("ts.execute.collectTicket.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Execute Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/execute/execute/{orderId}")
    @Timed(value = "ts.execute.executeTicket")
    public HttpEntity executeTicket(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        ExecuteControlller.LOGGER.info("[executeTicket][Execute][Id: {}]", orderId);
        // null
        Response<?> response = executeService.ticketExecute(orderId, headers);
        if (response.getStatus() != 1)
            executeTicket_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/execute/collected/{orderId}")
    @Timed(value = "ts.execute.collectTicket")
    public HttpEntity collectTicket(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        ExecuteControlller.LOGGER.info("[collectTicket][Collect][Id: {}]", orderId);
        // null
        Response<?> response = executeService.ticketCollect(orderId, headers);
        if (response.getStatus() != 1)
            collectTicket_error.increment();
        return ok(response);
    }
}
