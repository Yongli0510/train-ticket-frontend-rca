package consign.controller;

import consign.entity.Consign;
import consign.service.ConsignService;
import edu.fudan.common.util.Response;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.TimedAspect;
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
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/consignservice")
public class ConsignController {
    @Autowired
    ConsignService service;

    private static final Logger logger = LoggerFactory.getLogger(ConsignController.class);

    private Counter findByAccountId_error;
    private Counter findByOrderId_error;
    private Counter findByConsignee_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-consign-service");
        meterRegistry.config().commonTags(tags);
        findByAccountId_error = Counter.builder("ts.consign.findByAccountId.error").register(meterRegistry);
        findByOrderId_error = Counter.builder("ts.consign.findByOrderId.error").register(meterRegistry);
        findByConsignee_error = Counter.builder("ts.consign.findByConsignee.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Consign Service ] !";
    }

    @PostMapping(value = "/consigns")
    @Timed(value = "ts.consign.insertConsign")
    public HttpEntity insertConsign(@RequestBody Consign request,
                                    @RequestHeader HttpHeaders headers) {
        logger.info("[insertConsign][Insert consign record][id:{}]", request.getId());
        return ok(service.insertConsignRecord(request, headers));
    }

    @PutMapping(value = "/consigns")
    @Timed(value = "ts.consign.updateConsign")
    public HttpEntity updateConsign(@RequestBody Consign request, @RequestHeader HttpHeaders headers) {
        logger.info("[updateConsign][Update consign record][id: {}]", request.getId());
        return ok(service.updateConsignRecord(request, headers));
    }

    @GetMapping(value = "/consigns/account/{id}")
    @Timed(value = "ts.consign.findByAccountId")
    public HttpEntity findByAccountId(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        logger.info("[findByAccountId][Find consign by account id][id: {}]", id);
        UUID newid = UUID.fromString(id);
        Response<?> response = service.queryByAccountId(newid, headers);
        if (response.getStatus() != 1)
            findByAccountId_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/consigns/order/{id}")
    @Timed(value = "ts.consign.findByOrderId")
    public HttpEntity findByOrderId(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        logger.info("[findByOrderId][Find consign by order id][id: {}]", id);
        UUID newid = UUID.fromString(id);
        Response<?> response = service.queryByOrderId(newid, headers);
        if (response.getStatus() != 1)
            findByOrderId_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/consigns/{consignee}")
    @Timed(value = "ts.consign.findByConsignee")
    public HttpEntity findByConsignee(@PathVariable String consignee, @RequestHeader HttpHeaders headers) {
        logger.info("[findByConsignee][Find consign by consignee][consignee: {}]", consignee);
        Response<?> response = service.queryByConsignee(consignee, headers);
        if (response.getStatus() != 1)
            findByConsignee_error.increment();
        return ok(response);
    }
}
