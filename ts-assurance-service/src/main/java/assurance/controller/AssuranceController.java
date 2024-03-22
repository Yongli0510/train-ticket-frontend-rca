package assurance.controller;

import assurance.service.AssuranceService;
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
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/assuranceservice")
public class AssuranceController {

    @Autowired
    private AssuranceService assuranceService;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter get_all_assurances_error;
    private Counter get_all_assurance_type_error;
    private Counter delete_assurance_error;
    private Counter delete_assurance_by_order_id_error;
    private Counter modify_assurance_error;
    private Counter create_new_assurance_error;
    private Counter get_assurance_by_id_error;
    private Counter find_assurance_by_order_id_error;
    private static final Logger LOGGER = LoggerFactory.getLogger(AssuranceController.class);

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-assurance-service");
        meterRegistry.config().commonTags(tags);
        get_all_assurances_error = Counter.builder("ts.assurance.getAllAssurances.error").register(meterRegistry);
        get_all_assurance_type_error = Counter.builder("ts.assurance.getAllAssuranceType.error").register(meterRegistry);
        delete_assurance_error = Counter.builder("ts.assurance.deleteAssurance.error").register(meterRegistry);
        delete_assurance_by_order_id_error = Counter.builder("ts.assurance.deleteAssuranceByOrderId.error").register(meterRegistry);
        modify_assurance_error = Counter.builder("ts.assurance.modifyAssurance.error").register(meterRegistry);
        create_new_assurance_error = Counter.builder("ts.assurance.createNewAssurance.error").register(meterRegistry);
        get_assurance_by_id_error = Counter.builder("ts.assurance.getAssuranceById.error").register(meterRegistry);
        find_assurance_by_order_id_error = Counter.builder("ts.assurance.find.assurance.by.order.id.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Assurance Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/assurances")
    @Timed(value = "ts.assurance.getAllAssurances")
    public HttpEntity getAllAssurances(@RequestHeader HttpHeaders headers) {
        AssuranceController.LOGGER.info("[getAllAssurances][Get All Assurances]");
        Response<?> response = assuranceService.getAllAssurances(headers);
        if (response.getStatus() != 1)
            get_all_assurances_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/assurances/types")
    @Timed(value = "ts.assurance.getAllAssuranceType")
    public HttpEntity getAllAssuranceType(@RequestHeader HttpHeaders headers) {
        AssuranceController.LOGGER.info("[getAllAssuranceType][Get Assurance Type]");
        Response<?> response = assuranceService.getAllAssuranceTypes(headers);
        if (response.getStatus() != 1)
            get_all_assurance_type_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping(path = "/assurances/assuranceid/{assuranceId}")
    @Timed(value = "ts.assurance.deleteAssurance")
    public HttpEntity deleteAssurance(@PathVariable String assuranceId, @RequestHeader HttpHeaders headers) {
        AssuranceController.LOGGER.info("[deleteAssurance][Delete Assurance][assuranceId: {}]", assuranceId);
        Response<?> response = assuranceService.deleteById(UUID.fromString(assuranceId), headers);
        if (response.getStatus() != 1)
            delete_assurance_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping(path = "/assurances/orderid/{orderId}")
    @Timed(value = "ts.assurance.deleteAssuranceByOrderId")
    public HttpEntity deleteAssuranceByOrderId(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        AssuranceController.LOGGER.info("[deleteAssuranceByOrderId][Delete Assurance by orderId][orderId: {}]", orderId);
        Response<?> response = assuranceService.deleteByOrderId(UUID.fromString(orderId), headers);
        if (response.getStatus() != 1)
            delete_assurance_by_order_id_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PatchMapping(path = "/assurances/{assuranceId}/{orderId}/{typeIndex}")
    @Timed(value = "ts.assurance.modifyAssurance")
    public HttpEntity modifyAssurance(@PathVariable String assuranceId,
                                      @PathVariable String orderId,
                                      @PathVariable int typeIndex, @RequestHeader HttpHeaders headers) {
        AssuranceController.LOGGER.info("[modifyAssurance][Modify Assurance][assuranceId: {}, orderId: {}, typeIndex: {}]",
                assuranceId, orderId, typeIndex);
        Response<?> response = assuranceService.modify(assuranceId, orderId, typeIndex, headers);
        if (response.getStatus() != 1)
            modify_assurance_error.increment();
        return ok(response);
    }


    @CrossOrigin(origins = "*")
    @GetMapping(path = "/assurances/{typeIndex}/{orderId}")
    @Timed(value = "ts.assurance.createNewAssurance")
    public HttpEntity createNewAssurance(@PathVariable int typeIndex, @PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        //Assurance
        AssuranceController.LOGGER.info("[createNewAssurance][Create new assurance][typeIndex: {}, orderId: {}]", typeIndex, orderId);
        Response<?> response = assuranceService.create(typeIndex, orderId, headers);
        if (response.getStatus() != 1)
            create_new_assurance_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/assurances/assuranceid/{assuranceId}")
    @Timed(value = "ts.assurance.getAssuranceById")
    public HttpEntity getAssuranceById(@PathVariable String assuranceId, @RequestHeader HttpHeaders headers) {
        AssuranceController.LOGGER.info("[getAssuranceById][Find assurance by assuranceId][assureId: {}]", assuranceId);
        Response<?> response = assuranceService.findAssuranceById(UUID.fromString(assuranceId), headers);
        if (response.getStatus() != 1)
            get_assurance_by_id_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/assurance/orderid/{orderId}")
    @Timed(value = "ts.assurance.findAssuranceByOrderId")
    public HttpEntity findAssuranceByOrderId(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        AssuranceController.LOGGER.info("[findAssuranceByOrderId][Find assurance by orderId][orderId: {}]", orderId);
        Response<?> response = assuranceService.findAssuranceByOrderId(UUID.fromString(orderId), headers);
        if (response.getStatus() != 1)
            find_assurance_by_order_id_error.increment();
        return ok(response);
    }
}
