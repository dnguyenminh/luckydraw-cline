package vn.com.fecredit.app.monitoring;

import java.io.File;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import lombok.Data;

@TestConfiguration
@Profile("test")
@EnableConfigurationProperties(TestMonitoringConfig.MonitoringProperties.class)
public class TestMonitoringConfig {

    @Bean
    public EventStatisticsMonitor eventStatisticsMonitor(MonitoringProperties properties) {
        EventStatisticsMonitor monitor = EventStatisticsMonitor.getInstance();
        monitor.enableMonitoring();
        
        // Configure monitor thresholds
        monitor.setPerformanceThreshold("latencyWarning", 
            properties.getThresholds().getLatency().getWarning());
        monitor.setPerformanceThreshold("latencyCritical", 
            properties.getThresholds().getLatency().getCritical());
        monitor.setPerformanceThreshold("minimumThroughput", 
            properties.getThresholds().getThroughput().getMinimum());
        
        return monitor;
    }

    @Bean
    public PerformanceReporter performanceReporter(MonitoringProperties properties) {
        PerformanceReporter reporter = new PerformanceReporter();
        
        // Configure report settings
        File reportDir = new File(properties.getReport().getDirectory());
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }
        reporter.setReportDirectory(reportDir.getAbsolutePath());
        
        // Add reporter to monitor as a MetricsReporter
        EventStatisticsMonitor.getInstance().addReporter(reporter);
        
        return reporter;
    }

    @ConfigurationProperties(prefix = "monitoring.performance")
    @Data
    public static class MonitoringProperties {
        private ReportConfig report = new ReportConfig();
        private ThresholdConfig thresholds = new ThresholdConfig();
        private SamplingConfig sampling = new SamplingConfig();
        private AggregationConfig aggregation = new AggregationConfig();

        @Data
        public static class ReportConfig {
            private String directory = System.getProperty("java.io.tmpdir");
            private List<String> formats;
        }

        @Data
        public static class ThresholdConfig {
            private LatencyThreshold latency = new LatencyThreshold();
            private ThroughputThreshold throughput = new ThroughputThreshold();
            private MemoryThreshold memory = new MemoryThreshold();

            @Data
            public static class LatencyThreshold {
                private long warning = 100;
                private long critical = 500;
            }

            @Data
            public static class ThroughputThreshold {
                private long minimum = 1000;
            }

            @Data
            public static class MemoryThreshold {
                private long maximumPerOperation = 1024 * 1024;
            }
        }

        @Data
        public static class SamplingConfig {
            private boolean enabled = true;
            private double rate = 1.0;
        }

        @Data
        public static class AggregationConfig {
            private long interval = 1000;
            private long windowSize = 10000;
        }
    }

    @Bean
    public MonitoringMetricsValidator metricsValidator(MonitoringProperties properties) {
        return new MonitoringMetricsValidator(properties);
    }

    public static class MonitoringMetricsValidator {
        private final MonitoringProperties properties;

        public MonitoringMetricsValidator(MonitoringProperties properties) {
            this.properties = properties;
        }

        public boolean isLatencyAcceptable(double latency) {
            return latency <= properties.getThresholds().getLatency().getWarning();
        }

        public boolean isThroughputAcceptable(double throughput) {
            return throughput >= properties.getThresholds().getThroughput().getMinimum();
        }

        public boolean isMemoryUsageAcceptable(long bytes) {
            return bytes <= properties.getThresholds().getMemory().getMaximumPerOperation();
        }

        public boolean shouldSample() {
            if (!properties.getSampling().isEnabled()) {
                return true;
            }
            return Math.random() < properties.getSampling().getRate();
        }

        public long getAggregationInterval() {
            return properties.getAggregation().getInterval();
        }

        public long getAggregationWindowSize() {
            return properties.getAggregation().getWindowSize();
        }
    }
}
