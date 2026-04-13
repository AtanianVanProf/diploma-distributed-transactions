package am.diploma.saga.choreography.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${services.inventory.url}")
    private String inventoryUrl;

    @Value("${services.payment.url}")
    private String paymentUrl;

    @Bean
    public RestClient inventoryRestClient() {
        return RestClient.builder().baseUrl(inventoryUrl).build();
    }

    @Bean
    public RestClient paymentRestClient() {
        return RestClient.builder().baseUrl(paymentUrl).build();
    }
}
