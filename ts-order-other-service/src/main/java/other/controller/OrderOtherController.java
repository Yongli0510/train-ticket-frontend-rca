package other.controller;

import edu.fudan.common.entity.Seat;
import edu.fudan.common.util.Response;
import edu.fudan.common.util.StringUtils;
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


import other.entity.Order;
import other.entity.QueryInfo;
import other.service.OrderOtherService;

import javax.annotation.PostConstruct;
import java.util.Date;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/orderOtherService")
public class OrderOtherController {

    @Autowired
    private OrderOtherService orderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderOtherController.class);

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

        getTicketListByDateAndTripId_error = Counter.builder("ts.order-other.getTicketListByDateAndTripId.error").register(meterRegistry);
        createNewOrder_error = Counter.builder("ts.order-other.createNewOrder.error").register(meterRegistry);
        adminCreateNewOrder_error = Counter.builder("ts.order-other.adminCreateNewOrder.error").register(meterRegistry);
        queryOrders_error = Counter.builder("ts.order-other.queryOrders.error").register(meterRegistry);
        queryOrdersForRefresh_error = Counter.builder("ts.order-other.queryOrdersForRefresh.error").register(meterRegistry);
        getOrderPrice_error = Counter.builder("ts.order-other.getOrderPrice.error").register(meterRegistry);
        payOrder_error = Counter.builder("ts.order-other.payOrder.error").register(meterRegistry);
        getOrderById_error = Counter.builder("ts.order-other.getOrderById.error").register(meterRegistry);
        modifyOrder_error = Counter.builder("ts.order-other.modifyOrder.error").register(meterRegistry);
        saveOrderInfo_error = Counter.builder("ts.order-other.saveOrderInfo.error").register(meterRegistry);
        updateOrder_error = Counter.builder("ts.order-other.updateOrder.error").register(meterRegistry);
        deleteOrder_error = Counter.builder("ts.order-other.deleteOrder.error").register(meterRegistry);
        findAllOrder_error = Counter.builder("ts.order-other.findAllOrder.error").register(meterRegistry);
    }

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ Order Other Service ] !";
    }

    /***************************For Normal Use***************************/

    @PostMapping(value = "/orderOther/tickets")
    @Timed(value = "ts.order-other.getTicketListByDateAndTripId")
    public HttpEntity getTicketListByDateAndTripId(@RequestBody Seat seatRequest, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[getSoldTickets][Get Sold Ticket][Travel Date: {}]", seatRequest.getTravelDate().toString());
        Response<?> response = orderService.getSoldTickets(seatRequest, headers);
        if (response.getStatus() != 1)
            getTicketListByDateAndTripId_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/orderOther")
    @Timed(value = "ts.order-other.createNewOrder")
    public HttpEntity createNewOrder(@RequestBody Order createOrder, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[create][Create Order][from {} to {} at {}]", createOrder.getFrom(), createOrder.getTo(), createOrder.getTravelDate());
        Response<?> response = orderService.create(createOrder, headers);
        if (response.getStatus() != 1)
            createNewOrder_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/orderOther/admin")
    @Timed(value = "ts.order-other.addcreateNewOrder")
    public HttpEntity addcreateNewOrder(@RequestBody Order order, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[addNewOrder][Add new order][OrderId: {}]", order.getId());
        Response<?> response = orderService.addNewOrder(order, headers);
        if (response.getStatus() != 1)
            adminCreateNewOrder_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/orderOther/query")
    @Timed(value = "ts.order-other.queryOrders")
    public HttpEntity queryOrders(@RequestBody QueryInfo qi,
                                  @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[queryOrders][Query Orders][for LoginId :{}]", qi.getLoginId());
        Response<?> response = orderService.queryOrders(qi, qi.getLoginId(), headers);
        if (response.getStatus() != 1)
            queryOrders_error.increment();
        return ok(response);

    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/orderOther/refresh")
    @Timed(value = "ts.order-other.queryOrdersForRefresh")
    public HttpEntity queryOrdersForRefresh(@RequestBody QueryInfo qi,
                                            @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[queryOrdersForRefresh][Query Orders][for LoginId:{}]", qi.getLoginId());
        Response<?> response = orderService.queryOrdersForRefresh(qi, qi.getLoginId(), headers);
        if (response.getStatus() != 1)
            queryOrdersForRefresh_error.increment();
        return ok(response);
    }


    @CrossOrigin(origins = "*")
    @GetMapping(path = "/orderOther/{travelDate}/{trainNumber}")
    @Timed(value = "ts.order-other.calculateSoldTicket")
    public HttpEntity calculateSoldTicket(@PathVariable String travelDate, @PathVariable String trainNumber,
                                          @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[queryAlreadySoldOrders][Calculate Sold Tickets][Date: {} TrainNumber: {}]", travelDate, trainNumber);
        return ok(orderService.queryAlreadySoldOrders(StringUtils.String2Date(travelDate), trainNumber, headers));
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/orderOther/price/{orderId}")
    @Timed(value = "ts.order-other.getOrderPrice")
    public HttpEntity getOrderPrice(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[getOrderPrice][Get Order Price][Order Id: {}]", orderId);
        Response<?> response = orderService.getOrderPrice(orderId, headers);
        if (response.getStatus() != 1)
            getOrderPrice_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/orderOther/orderPay/{orderId}")
    @Timed(value = "ts.order-other.payOrder")
    public HttpEntity payOrder(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[payOrder][Pay Order][Order Id: {}]", orderId);
        Response<?> response = orderService.payOrder(orderId, headers);
        if (response.getStatus() != 1)
            payOrder_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/orderOther/{orderId}")
    @Timed(value = "ts.order-other.getOrderById")
    public HttpEntity getOrderById(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[getOrderById][Get Order By Id][Order Id: {}]", orderId);
        Response<?> response = orderService.getOrderById(orderId, headers);
        if (response.getStatus() != 1)
            getOrderById_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/orderOther/status/{orderId}/{status}")
    @Timed(value = "ts.order-other.modifyOrder")
    public HttpEntity modifyOrder(@PathVariable String orderId, @PathVariable int status, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[modifyOrder][Modify Order Status][Order Id: {}]", orderId);
        Response<?> response = orderService.modifyOrder(orderId, status, headers);
        if (response.getStatus() != 1)
            modifyOrder_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/orderOther/security/{checkDate}/{accountId}")
    @Timed(value = "ts.order-other.securityInfoCheck")
    public HttpEntity securityInfoCheck(@PathVariable String checkDate, @PathVariable String accountId,
                                        @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[checkSecurityAboutOrder][Security Info Get][CheckDate:{} , AccountId:{}]", checkDate, accountId);
        return ok(orderService.checkSecurityAboutOrder(StringUtils.String2Date(checkDate), accountId, headers));
    }

    @CrossOrigin(origins = "*")
    @PutMapping(path = "/orderOther")
    @Timed(value = "ts.order-other.saveOrderInfo")
    public HttpEntity saveOrderInfo(@RequestBody Order orderInfo,
                                    @RequestHeader HttpHeaders headers) {

        OrderOtherController.LOGGER.info("[saveChanges][Save Order Info][OrderId:{}]", orderInfo.getId());
        Response<?> response = orderService.saveChanges(orderInfo, headers);
        if (response.getStatus() != 1)
            saveOrderInfo_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PutMapping(path = "/orderOther/admin")
    @Timed(value = "ts.order-other.updateOrder")
    public HttpEntity updateOrder(@RequestBody Order order, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[updateOrder][Update Order][OrderId: {}]", order.getId());
        Response<?> response = orderService.updateOrder(order, headers);
        if (response.getStatus() != 1)
            updateOrder_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping(path = "/orderOther/{orderId}")
    @Timed(value = "ts.order-other.deleteOrder")
    public HttpEntity deleteOrder(@PathVariable String orderId, @RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[deleteOrder][Delete Order][OrderId: {}]", orderId);
        Response<?> response = orderService.deleteOrder(orderId, headers);
        if (response.getStatus() != 1)
            deleteOrder_error.increment();
        return ok(response);
    }

    /***************For super admin(Single Service Test*******************/

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/orderOther")
    @Timed(value = "ts.order-other.findAllOrder")
    public HttpEntity findAllOrder(@RequestHeader HttpHeaders headers) {
        OrderOtherController.LOGGER.info("[getAllOrders][Find All Order]");
        Response<?> response = orderService.getAllOrders(headers);
        if (response.getStatus() != 1)
            findAllOrder_error.increment();
        return ok(response);
    }
}
