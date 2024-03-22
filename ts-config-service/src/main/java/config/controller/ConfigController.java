package config.controller;

import config.entity.Config;
import config.service.ConfigService;
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


import javax.annotation.PostConstruct;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author Chenjie Xu
 * @date 2017/5/11.
 */
@RestController
@RequestMapping("api/v1/configservice")
public class ConfigController {
    @Autowired
    private ConfigService configService;

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private Counter queryAll_error;
    private Counter createConfig_error;
    private Counter updateConfig_error;
    private Counter deleteConfig_error;
    private Counter retrieve_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-config-service");
        meterRegistry.config().commonTags(tags);
        queryAll_error = Counter.builder("ts.config.queryAll.error").register(meterRegistry);
        createConfig_error = Counter.builder("ts.config.createConfig.error").register(meterRegistry);
        updateConfig_error = Counter.builder("ts.config.updateConfig.error").register(meterRegistry);
        deleteConfig_error = Counter.builder("ts.config.deleteConfig.error").register(meterRegistry);
        retrieve_error = Counter.builder("ts.config.retrieve.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Config Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/configs")
    @Timed(value = "ts.config.queryAll")
    public HttpEntity queryAll(@RequestHeader HttpHeaders headers) {
        logger.info("[queryAll][Query all configs]");
        Response<?> response = configService.queryAll(headers);
        if(response.getStatus() != 1)
            queryAll_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/configs")
    @Timed(value = "ts.config.createConfig")
    public HttpEntity<?> createConfig(@RequestBody Config info, @RequestHeader HttpHeaders headers) {
        logger.info("[createConfig][Create config][Config name: {}]", info.getName());
        Response<?> response = configService.create(info, headers);
        if(response.getStatus() != 1)
            createConfig_error.increment();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(value = "/configs")
    @Timed(value = "ts.config.updateConfig")
    public HttpEntity updateConfig(@RequestBody Config info, @RequestHeader HttpHeaders headers) {
        logger.info("[updateConfig][Update config][Config name: {}]", info.getName());
        Response<?> response = configService.update(info, headers);
        if(response.getStatus() != 1)
            updateConfig_error.increment();
        return ok(response);
    }


    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/configs/{configName}")
    @Timed(value = "ts.config.deleteConfig")
    public HttpEntity deleteConfig(@PathVariable String configName, @RequestHeader HttpHeaders headers) {
        logger.info("[deleteConfig][Delete config][configName: {}]", configName);
        Response<?> response = configService.delete(configName, headers);
        if(response.getStatus() != 1)
            deleteConfig_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/configs/{configName}")
    @Timed(value = "ts.config.retrieve")
    public HttpEntity retrieve(@PathVariable String configName, @RequestHeader HttpHeaders headers) {
        logger.info("[retrieve][Retrieve config][configName: {}]", configName);
        Response<?> response = configService.query(configName, headers);
        if(response.getStatus() != 1)
            retrieve_error.increment();
        return ok(response);
    }
}
