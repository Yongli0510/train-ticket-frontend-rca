package inside_payment.controller;

import edu.fudan.common.util.Response;
import inside_payment.entity.*;
import inside_payment.service.InsidePaymentService;
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
@RequestMapping("/api/v1/inside_pay_service")
public class InsidePaymentController {

    @Autowired
    public InsidePaymentService service;

    private static final Logger LOGGER = LoggerFactory.getLogger(InsidePaymentController.class);

    private Counter pay_error;
    private Counter createAccount_error;
    private Counter addMoney_error;
    private Counter queryPayment_error;
    private Counter drawBack_error;
    private Counter payDifference_error;
    private Counter queryAddMoney_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-inside-payment-service");
        meterRegistry.config().commonTags(tags);
        pay_error = Counter.builder("ts.inside-payment.pay.error").register(meterRegistry);
        createAccount_error = Counter.builder("ts.inside-payment.createAccount.error").register(meterRegistry);
        addMoney_error = Counter.builder("ts.inside-payment.addMoney.error").register(meterRegistry);
        queryPayment_error = Counter.builder("ts.inside-payment.queryPayment.error").register(meterRegistry);
        drawBack_error = Counter.builder("ts.inside-payment.drawBack.error").register(meterRegistry);
        payDifference_error = Counter.builder("ts.inside-payment.payDifference.error").register(meterRegistry);
        queryAddMoney_error = Counter.builder("ts.inside-payment.queryAddMoney.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ InsidePayment Service ] !";
    }

    @PostMapping(value = "/inside_payment")
    @Timed(value = "ts.inside-payment.pay")
    public HttpEntity pay(@RequestBody PaymentInfo info, @RequestHeader HttpHeaders headers) {
        InsidePaymentController.LOGGER.info("[pay][Inside Payment Service.Pay][Pay for: {}]", info.getOrderId());
        Response<?> response = service.pay(info, headers);
        if (response.getStatus() != 1)
            pay_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/inside_payment/account")
    @Timed(value = "ts.inside-payment.createAccount")
    public HttpEntity createAccount(@RequestBody AccountInfo info, @RequestHeader HttpHeaders headers) {
        LOGGER.info("[createAccount][Create account][accountInfo: {}]", info);
        Response<?> response = service.createAccount(info, headers);
        if (response.getStatus() != 1)
            createAccount_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/inside_payment/{userId}/{money}")
    @Timed(value = "ts.inside-payment.addMoney")
    public HttpEntity addMoney(@PathVariable String userId, @PathVariable
    String money, @RequestHeader HttpHeaders headers) {
        LOGGER.info("[addMoney][add money][userId: {}, money: {}]", userId, money);
        Response<?> response = service.addMoney(userId, money, headers);
        if (response.getStatus() != 1)
            addMoney_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/inside_payment/payment")
    @Timed(value = "ts.inside-payment.queryPayment")
    public HttpEntity queryPayment(@RequestHeader HttpHeaders headers) {
        LOGGER.info("[queryPayment][query payment]");
        Response<?> response = service.queryPayment(headers);
        if (response.getStatus() != 1)
            queryPayment_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/inside_payment/account")
    @Timed(value = "ts.inside-payment.queryAccount")
    public HttpEntity queryAccount(@RequestHeader HttpHeaders headers) {
        LOGGER.info("[queryAccount][query account]");
        return ok(service.queryAccount(headers));
    }

    @GetMapping(value = "/inside_payment/drawback/{userId}/{money}")
    @Timed(value = "ts.inside-payment.drawBack")
    public HttpEntity drawBack(@PathVariable String userId, @PathVariable String money, @RequestHeader HttpHeaders headers) {
        LOGGER.info("[drawBack][draw back payment][userId: {}, money: {}]", userId, money);
        Response<?> response = service.drawBack(userId, money, headers);
        if (response.getStatus() != 1)
            drawBack_error.increment();
        return ok(response);
    }

    @PostMapping(value = "/inside_payment/difference")
    @Timed(value = "ts.inside-payment.payDifference")
    public HttpEntity payDifference(@RequestBody PaymentInfo info, @RequestHeader HttpHeaders headers) {
        LOGGER.info("[payDifference][pay difference]");
        Response<?> response = service.payDifference(info, headers);
        if (response.getStatus() != 1)
            payDifference_error.increment();
        return ok(response);
    }

    @GetMapping(value = "/inside_payment/money")
    @Timed(value = "ts.inside-payment.queryAddMoney")
    public HttpEntity queryAddMoney(@RequestHeader HttpHeaders headers) {
        LOGGER.info("[queryAddMoney][query add money]");
        Response<?> response = service.queryAddMoney(headers);
        if (response.getStatus() != 1)
            queryAddMoney_error.increment();
        return ok(response);
    }
}
