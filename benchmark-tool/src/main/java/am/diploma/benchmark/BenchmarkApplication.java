package am.diploma.benchmark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import am.diploma.benchmark.config.BenchmarkProperties;

@SpringBootApplication
@EnableConfigurationProperties(BenchmarkProperties.class)
public class BenchmarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(BenchmarkApplication.class, args);
    }
}
