package am.diploma.pvip.protocol.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic intentRequestsTopic() {
        return new NewTopic("pvip.intent-requests", 1, (short) 1);
    }

    @Bean
    public NewTopic intentResponsesTopic() {
        return new NewTopic("pvip.intent-responses", 1, (short) 1);
    }

    @Bean
    public NewTopic decisionsTopic() {
        return new NewTopic("pvip.decisions", 1, (short) 1);
    }
}
