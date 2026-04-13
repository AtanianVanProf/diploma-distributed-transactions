package am.diploma.saga.choreography.order.controller;

import am.diploma.saga.choreography.order.dto.OrderResponse;
import am.diploma.saga.choreography.order.dto.PlaceOrderRequest;
import am.diploma.saga.choreography.order.dto.PlaceOrderResponse;
import am.diploma.saga.choreography.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        PlaceOrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
}
