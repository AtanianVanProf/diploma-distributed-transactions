package am.diploma.microservices.naive.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveStockResponse {

    private boolean reserved;
    private List<ReservedItemResponse> items;
}
