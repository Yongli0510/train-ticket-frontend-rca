package security.controller;

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
import security.entity.*;
import security.service.SecurityService;

import javax.annotation.PostConstruct;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/securityservice")
public class SecurityController {
    @Autowired
    private SecurityService securityService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityController.class);

    private Counter findAllSecurityConfig_error;
    private Counter create_error;
    private Counter update_error;
    private Counter delete_error;
    private Counter check_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-security-service");
        meterRegistry.config().commonTags(tags);
        findAllSecurityConfig_error = Counter.builder("ts.security.findAllSecurityConfig.error").register(meterRegistry);
        create_error = Counter.builder("ts.security.create.error").register(meterRegistry);
        update_error = Counter.builder("ts.security.update.error").register(meterRegistry);
        delete_error = Counter.builder("ts.security.delete.error").register(meterRegistry);
        check_error = Counter.builder("ts.security.check.error").register(meterRegistry);
    }

    @GetMapping(value = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "welcome to [Security Service]";
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/securityConfigs")
    @Timed(value = "ts.security.findAllSecurityConfig")
    public HttpEntity findAllSecurityConfig(@RequestHeader HttpHeaders headers) {
        SecurityController.LOGGER.info("[findAllSecurityConfig][Find All]");
        Response<?> response = securityService.findAllSecurityConfig(headers);
        if (response.getStatus() != 1)
            findAllSecurityConfig_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/securityConfigs")
    @Timed(value = "ts.security.create")
    public HttpEntity create(@RequestBody SecurityConfig info, @RequestHeader HttpHeaders headers) {
        SecurityController.LOGGER.info("[addNewSecurityConfig][Create][SecurityConfig Name: {}]", info.getName());
        Response<?> response = securityService.addNewSecurityConfig(info, headers);
        if (response.getStatus() != 1)
            create_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(path = "/securityConfigs")
    @Timed(value = "ts.security.update")
    public HttpEntity update(@RequestBody SecurityConfig info, @RequestHeader HttpHeaders headers) {
        SecurityController.LOGGER.info("[modifySecurityConfig][Update][SecurityConfig Name: {}]", info.getName());
        Response<?> response = securityService.modifySecurityConfig(info, headers);
        if (response.getStatus() != 1)
            update_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping(path = "/securityConfigs/{id}")
    @Timed(value = "ts.security.delete")
    public HttpEntity delete(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        SecurityController.LOGGER.info("[deleteSecurityConfig][Delete][SecurityConfig Id: {}]", id);
        Response<?> response = securityService.deleteSecurityConfig(id, headers);
        if (response.getStatus() != 1)
            delete_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/securityConfigs/{accountId}")
    @Timed(value = "ts.security.check")
    public HttpEntity check(@PathVariable String accountId, @RequestHeader HttpHeaders headers) {
        SecurityController.LOGGER.info("[check][Check Security][Check Account Id: {}]", accountId);
        Response<?> response = securityService.check(accountId, headers);
        if (response.getStatus() != 1)
            check_error.increment();
        return ok(response);
    }
}
