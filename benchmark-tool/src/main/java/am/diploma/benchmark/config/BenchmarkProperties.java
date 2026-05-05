package am.diploma.benchmark.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "benchmark")
public class BenchmarkProperties {

    private Targets targets = new Targets();
    private Polling polling = new Polling();
    private Scenarios scenarios = new Scenarios();

    @Getter
    @Setter
    public static class Targets {
        private TargetConfig orchestrator = new TargetConfig();
        private TargetConfig choreography = new TargetConfig();
        private TargetConfig pvip = new TargetConfig();
    }

    @Getter
    @Setter
    public static class TargetConfig {
        private String baseUrl;
        private String placeOrderPath;
        private String sagasPath;
        private String sagaPath;
        private String protocolPath;
        private String resetPath;
    }

    @Getter
    @Setter
    public static class Polling {
        private long initialDelayMs = 100;
        private long maxDelayMs = 1000;
        private long timeoutMs = 30000;
    }

    @Getter
    @Setter
    public static class Scenarios {
        private int happyPathCount = 50;
        private int stockFailureCount = 20;
        private int balanceFailureCount = 20;
        private int timeoutCount = 10;
        private int resetBatchSize = 10;
    }
}
