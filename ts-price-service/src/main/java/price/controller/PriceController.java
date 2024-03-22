package price.controller;

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
import price.entity.PriceConfig;
import price.service.PriceService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/priceservice")
public class PriceController {

    @Autowired
    PriceService service;

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceController.class);

    private Counter queryByRouteIdAndTrainType_error;
    private Counter queryByRouteIdsAndTrainTypes_error;
    private Counter queryAll_error;
    private Counter delete_error;
    private Counter update_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-price-service");
        meterRegistry.config().commonTags(tags);
        queryByRouteIdAndTrainType_error = Counter.builder("ts.price.queryByRouteIdAndTrainType.error").register(meterRegistry);
        queryByRouteIdsAndTrainTypes_error = Counter.builder("ts.price.queryByRouteIdsAndTrainTypes.error").register(meterRegistry);
        queryAll_error = Counter.builder("ts.price.queryAll.error").register(meterRegistry);
        delete_error = Counter.builder("ts.price.delete.error").register(meterRegistry);
        update_error = Counter.builder("ts.price.update.error").register(meterRegistry);
    }

    @GetMapping(path = "/prices/welcome")
    public String home() {
        return "Welcome to [ Price Service ] !";
    }

    @GetMapping(value = "/prices/{routeId}/{trainType}")
    @Timed(value = "ts.price.queryByRouteIdAndTrainType")
    public HttpEntity queryByRouteIdAndTrainType(@PathVariable String routeId, @PathVariable String trainType,
                            @RequestHeader HttpHeaders headers) {
        PriceController.LOGGER.info("[findByRouteIdAndTrainType][Query price][RouteId: {}, TrainType: {}]", routeId, trainType);
        Response<?> response = service.findByRouteIdAndTrainType(routeId, trainType, headers);
        if (response.getStatus() != 1)
            queryByRouteIdAndTrainType_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/prices/byRouteIdsAndTrainTypes")
    @Timed(value = "ts.price.queryByRouteIdsAndTrainTypes")
    public HttpEntity queryByRouteIdsAndTrainTypes(@RequestBody List<String> ridsAndTts,
                            @RequestHeader HttpHeaders headers) {
        PriceController.LOGGER.info("[findByRouteIdAndTrainType][Query price][routeId and Train Type: {}]", ridsAndTts);
        Response<?> response = service.findByRouteIdsAndTrainTypes(ridsAndTts, headers);
        if (response.getStatus() != 1)
            queryByRouteIdsAndTrainTypes_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/prices")
    @Timed(value = "ts.price.queryAll")
    public HttpEntity queryAll(@RequestHeader HttpHeaders headers) {
        PriceController.LOGGER.info("[findAllPriceConfig][Query all prices]");
        Response<?> response = service.findAllPriceConfig(headers);
        if (response.getStatus() != 1)
            queryAll_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/prices")
    @Timed(value = "ts.price.create")
    public HttpEntity<?> create(@RequestBody PriceConfig info,
                                @RequestHeader HttpHeaders headers) {
        PriceController.LOGGER.info("[createNewPriceConfig][Create price][RouteId: {}, TrainType: {}]", info.getRouteId(), info.getTrainType());
        return new ResponseEntity<>(service.createNewPriceConfig(info, headers), HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/prices/{pricesId}")
    @Timed(value = "ts.price.delete")
    public HttpEntity delete(@PathVariable String pricesId, @RequestHeader HttpHeaders headers) {
        PriceController.LOGGER.info("[deletePriceConfig][Delete price][PriceConfigId: {}]", pricesId);
        Response<?> response = service.deletePriceConfig(pricesId, headers);
        if (response.getStatus() != 1)
            delete_error.increment();
        return ok(response);
    }

    @PutMapping(value = "/prices")
    @Timed(value = "ts.price.update")
    public HttpEntity update(@RequestBody PriceConfig info, @RequestHeader HttpHeaders headers) {
        PriceController.LOGGER.info("[updatePriceConfig][Update price][PriceConfigId: {}]", info.getId());
        Response<?> response = service.updatePriceConfig(info, headers);
        if (response.getStatus() != 1)
            update_error.increment();
        return ok(response);
    }
}
