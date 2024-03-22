package fdse.microservice.controller;

import edu.fudan.common.util.Response;
import fdse.microservice.entity.*;
import fdse.microservice.service.StationService;
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

import javax.annotation.PostConstruct;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/stationservice")
public class StationController {

    @Autowired
    private StationService stationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(StationController.class);

    private Counter query_error;
    private Counter create_error;
    private Counter update_error;
    private Counter delete_error;
    private Counter queryForStationId_error;
    private Counter queryForIdBatch_error;
    private Counter queryById_error;
    private Counter queryForNameBatch_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-station-service");
        meterRegistry.config().commonTags(tags);
        query_error = Counter.builder("ts.station.query.error").register(meterRegistry);
        create_error = Counter.builder("ts.station.create.error").register(meterRegistry);
        update_error = Counter.builder("ts.station.update.error").register(meterRegistry);
        delete_error = Counter.builder("ts.station.delete.error").register(meterRegistry);
        queryForStationId_error = Counter.builder("ts.station.queryForStationId.error").register(meterRegistry);
        queryForIdBatch_error = Counter.builder("ts.station.queryForIdBatch.error").register(meterRegistry);
        queryById_error = Counter.builder("ts.station.queryById.error").register(meterRegistry);
        queryForNameBatch_error = Counter.builder("ts.station.queryForNameBatch.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Station Service ] !";
    }

    @GetMapping(value = "/stations")
    @Timed(value = "ts.station.query")
    public HttpEntity query(@RequestHeader HttpHeaders headers) {
        Response<?> response = stationService.query(headers);
        if (response.getStatus() != 1)
            query_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/stations")
    @Timed(value = "ts.station.create")
    public ResponseEntity<Response> create(@RequestBody Station station, @RequestHeader HttpHeaders headers) {
        StationController.LOGGER.info("[create][Create station][name: {}]", station.getName());
        Response<?> response = stationService.create(station, headers);
        if (response.getStatus() != 1)
            create_error.increment();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/stations")
    @Timed(value = "ts.station.update")
    public HttpEntity update(@RequestBody Station station, @RequestHeader HttpHeaders headers) {
        StationController.LOGGER.info("[update][Update station][StationId: {}]", station.getId());
        Response<?> response = stationService.update(station, headers);
        if (response.getStatus() != 1)
            update_error.increment();
        return ok(response);
    }

    @DeleteMapping(value = "/stations/{stationsId}")
    @Timed(value = "ts.station.delete")
    public ResponseEntity<Response> delete(@PathVariable String stationsId, @RequestHeader HttpHeaders headers) {
        StationController.LOGGER.info("[delete][Delete station][StationId: {}]", stationsId);
        Response<?> response = stationService.delete(stationsId, headers);
        if (response.getStatus() != 1)
            delete_error.increment();
        return ok(response);
    }

    // according to station name ---> query station id
    @GetMapping(value = "/stations/id/{stationNameForId}")
    @Timed(value = "ts.station.queryForStationId")
    public HttpEntity queryForStationId(@PathVariable(value = "stationNameForId")
                                        String stationName, @RequestHeader HttpHeaders headers) {
        // string
        StationController.LOGGER.info("[queryForId][Query for station id][StationName: {}]", stationName);
        Response<?> response = stationService.queryForId(stationName, headers);
        if (response.getStatus() != 1)
            queryForStationId_error.increment();
        return ok(response);
    }

    // according to station name list --->  query all station ids
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/stations/idlist")
    @Timed(value = "ts.station.queryForIdBatch")
    public HttpEntity queryForIdBatch(@RequestBody List<String> stationNameList, @RequestHeader HttpHeaders headers) {
        StationController.LOGGER.info("[queryForIdBatch][Query stations for id batch][StationNameNumbers: {}]", stationNameList.size());
        Response<?> response = stationService.queryForIdBatch(stationNameList, headers);
        if (response.getStatus() != 1)
            queryForIdBatch_error.increment();
        return ok(response);
    }

    // according to station id ---> query station name
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/stations/name/{stationIdForName}")
    @Timed(value = "ts.station.queryById")
    public HttpEntity queryById(@PathVariable(value = "stationIdForName")
                                String stationId, @RequestHeader HttpHeaders headers) {
        StationController.LOGGER.info("[queryById][Query stations By Id][Id: {}]", stationId);
        // string
        Response<?> response = stationService.queryById(stationId, headers);
        if (response.getStatus() != 1)
            queryById_error.increment();
        return ok(response);
    }

    // according to station id list  ---> query all station names
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/stations/namelist")
    @Timed(value = "ts.station.queryForNameBatch")
    public HttpEntity queryForNameBatch(@RequestBody List<String> stationIdList, @RequestHeader HttpHeaders headers) {
        StationController.LOGGER.info("[queryByIdBatch][Query stations for name batch][StationIdNumbers: {}]", stationIdList.size());
        Response<?> response = stationService.queryByIdBatch(stationIdList, headers);
        if (response.getStatus() != 1)
            queryForNameBatch_error.increment();
        return ok(response);
    }
}
