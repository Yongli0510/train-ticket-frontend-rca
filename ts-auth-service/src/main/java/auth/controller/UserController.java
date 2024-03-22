package auth.controller;


import auth.dto.BasicAuthDto;
import auth.entity.User;
import auth.exception.UserOperationException;
import auth.service.TokenService;
import auth.service.UserService;
import edu.fudan.common.util.Response;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

import io.opentelemetry.api.trace.Span;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter login_error;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-auth-service");
        meterRegistry.config().commonTags(tags);
        login_error = Counter.builder("ts.auth.login.error").register(meterRegistry);
    }

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/hello")
    public Object getHello() {
        return "Hello";
    }

    @PostMapping("/login")
    @Timed(value = "ts.auth.login")
    public ResponseEntity<Response> login(@RequestBody BasicAuthDto dao, @RequestHeader HttpHeaders headers) {
        logger.info("Login request of username: {}", dao.getUsername());
        Span currentSpan = Span.current();
        currentSpan.setAttribute("username", dao.getUsername());
        try {
            Response<?> res = tokenService.getToken(dao, headers);
            if (res.getStatus() != 1)
                login_error.increment();
            return ResponseEntity.ok(res);
        } catch (UserOperationException e) {
            login_error.increment();
            logger.error("[getToken][tokenService.getToken error][UserOperationException, message: {}]", e.getMessage());
            return ResponseEntity.ok(new Response<>(0, "get token error", null));
        }
    }

    @GetMapping
    @Timed(value = "ts.auth.getAllUser")
    public ResponseEntity<List<User>> getAllUser(@RequestHeader HttpHeaders headers) {
        logger.info("[getAllUser][Get all users]");
        return ResponseEntity.ok().body(userService.getAllUser(headers));
    }

    @DeleteMapping("/{userId}")
    @Timed(value = "ts.auth.deleteUserById")
    public ResponseEntity<Response> deleteUserById(@PathVariable String userId, @RequestHeader HttpHeaders headers) {
        logger.info("[deleteUserById][Delete user][userId: {}]", userId);
        return ResponseEntity.ok(userService.deleteByUserId(userId, headers));
    }

}
