package route.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import route.entity.RouteInfo;
import route.service.RouteService;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/routeservice")
public class RouteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteController.class);
    @Autowired
    private RouteService routeService;
    private Counter createAndModifyRoute_error;
    private Counter deleteRoute_error;
    private Counter queryById_error;
    private Counter queryByIds_error;
    private Counter queryAll_error;
    private Counter queryByStartAndTerminal_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-route-service");
        meterRegistry.config().commonTags(tags);
        createAndModifyRoute_error = Counter.builder("ts.route.createAndModifyRoute.error").register(meterRegistry);
        deleteRoute_error = Counter.builder("ts.route.deleteRoute.error").register(meterRegistry);
        queryById_error = Counter.builder("ts.route.queryById.error").register(meterRegistry);
        queryByIds_error = Counter.builder("ts.route.queryByIds.error").register(meterRegistry);
        queryAll_error = Counter.builder("ts.route.queryAll.error").register(meterRegistry);
        queryByStartAndTerminal_error = Counter.builder("ts.route.queryByStartAndTerminal.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ Route Service ] !";
    }

    @PostMapping(path = "/routes")
    @Timed(value = "ts.route.createAndModifyRoute")
    public ResponseEntity<Response> createAndModifyRoute(@RequestBody RouteInfo createAndModifyRouteInfo, @RequestHeader HttpHeaders headers) {
        RouteController.LOGGER.info("[createAndModify][Create route][start: {}, end: {}]", createAndModifyRouteInfo.getStartStation(), createAndModifyRouteInfo.getEndStation());
        Response<?> response = routeService.createAndModify(createAndModifyRouteInfo, headers);
        if (response.getStatus() != 1)
            createAndModifyRoute_error.increment();
        return ok(response);
    }

    @DeleteMapping(path = "/routes/{routeId}")
    @Timed(value = "ts.route.deleteRoute")
    public HttpEntity deleteRoute(@PathVariable String routeId, @RequestHeader HttpHeaders headers) {
        RouteController.LOGGER.info("[deleteRoute][Delete route][RouteId: {}]", routeId);
        Response<?> response = routeService.deleteRoute(routeId, headers);
        if (response.getStatus() != 1)
            deleteRoute_error.increment();
        return ok(response);
    }

    @GetMapping(path = "/routes/{routeId}")
    @Timed(value = "ts.route.queryById")
    public HttpEntity queryById(@PathVariable String routeId, @RequestHeader HttpHeaders headers) {
        RouteController.LOGGER.info("[getRouteById][Query route by id][RouteId: {}]", routeId);
        Response<?> response = routeService.getRouteById(routeId, headers);
        if (response.getStatus() != 1)
            queryById_error.increment();
        return ok(response);
    }

    @PostMapping(path = "/routes/byIds")
    @Timed(value = "ts.route.queryByIds")
    public HttpEntity queryByIds(@RequestBody List<String> routeIds, @RequestHeader HttpHeaders headers) {
        RouteController.LOGGER.info("[getRouteById][Query route by id][RouteId: {}]", routeIds);
        Response<?> response = routeService.getRouteByIds(routeIds, headers);
        if (response.getStatus() != 1)
            queryByIds_error.increment();
        return ok(response);
    }

    @GetMapping(path = "/routes")
    @Timed(value = "ts.route.queryAll")
    public HttpEntity queryAll(@RequestHeader HttpHeaders headers) {
        RouteController.LOGGER.info("[getAllRoutes][Query all routes]");
        Response<?> response = routeService.getAllRoutes(headers);
        if (response.getStatus() != 1)
            queryAll_error.increment();
        return ok(response);
    }

    @GetMapping(path = "/routes/{start}/{end}")
    @Timed(value = "ts.route.queryByStartAndTerminal")
    public HttpEntity queryByStartAndTerminal(@PathVariable String start,
                                              @PathVariable String end,
                                              @RequestHeader HttpHeaders headers) {
        RouteController.LOGGER.info("[getRouteByStartAndEnd][Query routes][start: {}, end: {}]", start, end);
        Response<?> response = routeService.getRouteByStartAndEnd(start, end, headers);
        if (response.getStatus() != 1)
            queryByStartAndTerminal_error.increment();
        return ok(response);
    }
}