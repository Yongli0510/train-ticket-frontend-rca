package order.controller;

import edu.fudan.common.entity.Seat;
import edu.fudan.common.util.Response;
import edu.fudan.common.util.StringUtils;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import order.entity.*;
import order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Date;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/orderservice")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    private Counter getTicketListByDateAndTripId_error;
    private Counter createNewOrder_error;
    private Counter adminCreateNewOrder_error;
    private Counter queryOrders_error;
    private Counter queryOrdersForRefresh_error;
    private Counter getOrderPrice_error;
    private Counter payOrder_error;
    private Counter getOrderById_error;
    private Counter modifyOrder_error;
    private Counter saveOrderInfo_error;
    private Counter updateOrder_error;
    private Counter deleteOrder_error;
    private Counter findAllOrder_error;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-order-other-service");
        meterRegistry.config().commonTags(tags);

        getTicketListByDateAndTripId_error = Counter.builder("ts.order.getTicketListByDateAndTripId.error").register(meterRegistry);
        createNewOrder_error = Counter.builder("ts.order.createNewOrder.error").register(meterRegistry);
        adminCreateNewOrder_error = Counter.builder("ts.order.adminCreateNewOrder.error").register(meterRegistry);
        queryOrders_error = Counter.builder("ts.order.queryOrders.error").register(meterRegistry);
        queryOrdersForRefresh_error = Counter.builder("ts.order.queryOrdersForRefresh.error").register(meterRegistry);
        getOrderPrice_error = Counter.builder("ts.order.getOrderPrice.error").register(meterRegistry);
        payOrder_error = Counter.builder("ts.order.payOrder.error").register(meterRegistry);
        getOrderById_error = Counter.builder("ts.order.getOrderById.error").register(meterRegistry);
        modifyOrder_error = Counter.builder("ts.order.modifyOrder.error").register(meterRegistry);
        saveOrderInfo_error = Counter.builder("ts.order.saveOrderInfo.error").register(meterRegistry);
        updateOrder_error = Counter.builder("ts.order.updateOrder.error").register(meterRegistry);
        deleteOrder_error = Counter.builder("ts.order.deleteOrder.error").register(meterRegistry);
        findAllOrder_error = Counter.builder("ts.order.findAllOrder.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ Order Service ] !";
    }

    /***************************For Normal Use***************************/

    @PostMapping(value = "/order/tickets")
    @Timed(value = "ts.order.getTicketListByDateAndTripId")
    public HttpEntity getTicketListByDateAndTripId(@RequestBody Seat seatRequest, @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[getSoldTickets][Get Sold Ticket][Travel Date: {}]", seatRequest.getTravelDate().toString());
        Response<?> response = orderService.getSoldTickets(seatRequest, headers);
        if (response.getStatus() != 1)
            getTicketListByDateAndTripId_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/order")
    @Timed(value = "ts.order.createNewOrder")
    public HttpEntity createNewOrder(@RequestBody Order createOrder, @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[createNewOrder][Create Order][from {} to {} at {}]", createOrder.getFrom(), createOrder.getTo(), createOrder.getTravelDate());
        Response<?> response = orderService.create(createOrder, headers);
        if (response.getStatus() != 1)
            createNewOrder_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/order/admin")
    @Timed(value = "ts.order.addcreateNewOrder")
    public HttpEntity addcreateNewOrder(@RequestBody Order order, @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[addNewOrder][Add new order][OrderId: {}]", order.getId());
        Response<?> response = orderService.addNewOrder(order, headers);
        if (response.getStatus() != 1)
            adminCreateNewOrder_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/order/query")
    @Timed(value = "ts.order.queryOrders")
    public HttpEntity queryOrders(@RequestBody OrderInfo qi,
                                  @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[queryOrders][Query Orders][for LoginId :{}]", qi.getLoginId());
        Response<?> response = orderService.queryOrders(qi, qi.getLoginId(), headers);
        if (response.getStatus() != 1)
            queryOrders_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/order/refresh")
    @Timed(value = "ts.order.queryOrdersForRefresh")
    public HttpEntity queryOrdersForRefresh(@RequestBody OrderInfo qi,
                                            @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[queryOrdersForRefresh][Query Orders][for LoginId:{}]", qi.getLoginId());
        Response<?> response = orderService.queryOrdersForRefresh(qi, qi.getLoginId(), headers);
        if (response.getStatus() != 1)
            queryOrdersForRefresh_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/order/{travelDate}/{trainNumber}")
    @Timed(value = "ts.order.calculateSoldTicket")
    public HttpEntity calculateSoldTicket(@PathVariable String travelDate, @PathVariable String trainNumber,
                                          @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[queryAlreadySoldOrders][Calculate Sold Tickets][Date: {} TrainNumber: {}]", travelDate, trainNumber);
        return ok(orderService.queryAlreadySoldOrders(StringUtils.String2Date(travelDate), trainNumber, headers));
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/order/price/{orderId}")
    @Timed(value = "ts.order.getOrderPrice")
    public HttpEntity getOrderPrice(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[getOrderPrice][Get Order Price][OrderId: {}]", orderId);
        // String
        Response<?> response = orderService.getOrderPrice(orderId, headers);
        if (response.getStatus() != 1)
            getOrderPrice_error.increment();
        return ok(response);
    }


    @CrossOrigin(origins = "*")
    @GetMapping(path = "/order/orderPay/{orderId}")
    @Timed(value = "ts.order.payOrder")
    public HttpEntity payOrder(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[payOrder][Pay Order][OrderId: {}]", orderId);
        // Order
        Response<?> response = orderService.payOrder(orderId, headers);
        if (response.getStatus() != 1)
            payOrder_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/order/{orderId}")
    @Timed(value = "ts.order.getOrderById")
    public HttpEntity getOrderById(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[getOrderById][Get Order By Id][OrderId: {}]", orderId);
        // Order
        Response<?> response = orderService.getOrderById(orderId, headers);
        if (response.getStatus() != 1)
            getOrderById_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/order/status/{orderId}/{status}")
    @Timed(value = "ts.order.modifyOrder")
    public HttpEntity modifyOrder(@PathVariable String orderId, @PathVariable int status, @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[modifyOrder][Modify Order Status][OrderId: {}]", orderId);
        // Order
        Response<?> response = orderService.modifyOrder(orderId, status, headers);
        if (response.getStatus() != 1)
            modifyOrder_error.increment();
        return ok(response);
    }


    @CrossOrigin(origins = "*")
    @GetMapping(path = "/order/security/{checkDate}/{accountId}")
    @Timed(value = "ts.order.securityInfoCheck")
    public HttpEntity securityInfoCheck(@PathVariable String checkDate, @PathVariable String accountId,
                                        @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[checkSecurityAboutOrder][Security Info Get][AccountId:{}]", accountId);
        return ok(orderService.checkSecurityAboutOrder(StringUtils.String2Date(checkDate), accountId, headers));
    }


    @CrossOrigin(origins = "*")
    @PutMapping(path = "/order")
    @Timed(value = "ts.order.saveOrderInfo")
    public HttpEntity saveOrderInfo(@RequestBody Order orderInfo,
                                    @RequestHeader HttpHeaders headers) {

        OrderController.LOGGER.info("[saveChanges][Save Order Info][OrderId:{}]", orderInfo.getId());
        Response<?> response = orderService.saveChanges(orderInfo, headers);
        if (response.getStatus() != 1)
            saveOrderInfo_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(path = "/order/admin")
    @Timed(value = "ts.order.updateOrder")
    public HttpEntity updateOrder(@RequestBody Order order, @RequestHeader HttpHeaders headers) {
        // Order
        OrderController.LOGGER.info("[updateOrder][Update Order][OrderId: {}]", order.getId());
        Response<?> response = orderService.updateOrder(order, headers);
        if (response.getStatus() != 1)
            updateOrder_error.increment();
        return ok(response);
    }


    @CrossOrigin(origins = "*")
    @DeleteMapping(path = "/order/{orderId}")
    @Timed(value = "ts.order.deleteOrder")
    public HttpEntity deleteOrder(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[deleteOrder][Delete Order][OrderId: {}]", orderId);
        // Order
        Response<?> response = orderService.deleteOrder(orderId, headers);
        if (response.getStatus() != 1)
            deleteOrder_error.increment();
        return ok(response);
    }

    /***************For super admin(Single Service Test*******************/

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/order")
    @Timed(value = "ts.order.findAllOrder")
    public HttpEntity findAllOrder(@RequestHeader HttpHeaders headers) {
        OrderController.LOGGER.info("[getAllOrders][Find All Order]");
        // ArrayList<Order>
        Response<?> response = orderService.getAllOrders(headers);
        if (response.getStatus() != 1)
            findAllOrder_error.increment();
        return ok(response);
    }
}
