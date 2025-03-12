package vn.com.fecredit.app.performance.trend;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for PerformanceTrendAnalyzer.
 * Measures throughput and latency for various operations and data sizes.
 *
 * Run with: mvn clean install && java -jar target/benchmarks.jar
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class PerformanceTrendAnalyzerBenchmark {
    
    private PerformanceTrendAnalyzer analyzer;
    private List<DataPoint> smallDataset;
    private List<DataPoint> mediumDataset;
    private List<DataPoint> largeDataset;
    private List<DataPoint> volatileDataset;

    @Setup
    public void setup() {
        analyzer = new DefaultPerformanceTrendAnalyzer(AnalyzerConfig.getDefault());
        smallDataset = generateDataset(10, 100.0, 1.1);
        mediumDataset = generateDataset(100, 100.0, 1.05);
        largeDataset = generateDataset(1000, 100.0, 1.01);
        volatileDataset = generateVolatileDataset(100);
    }

    @Benchmark
    public void analyzeTrendSmallDataset(Blackhole blackhole) {
        blackhole.consume(analyzer.analyzeTrend(smallDataset));
    }

    @Benchmark
    public void analyzeTrendMediumDataset(Blackhole blackhole) {
        blackhole.consume(analyzer.analyzeTrend(mediumDataset));
    }

    @Benchmark
    public void analyzeTrendLargeDataset(Blackhole blackhole) {
        blackhole.consume(analyzer.analyzeTrend(largeDataset));
    }

    @Benchmark
    public void analyzeTrendVolatileDataset(Blackhole blackhole) {
        blackhole.consume(analyzer.analyzeTrend(volatileDataset));
    }

    @Benchmark
    public void calculateMovingAverageSmallWindow(Blackhole blackhole) {
        blackhole.consume(analyzer.calculateMovingAverage(mediumDataset, 3));
    }

    @Benchmark
    public void calculateMovingAverageLargeWindow(Blackhole blackhole) {
        blackhole.consume(analyzer.calculateMovingAverage(mediumDataset, 20));
    }

    @Benchmark
    public void multipleOperations(Blackhole blackhole) {
        var trend = analyzer.analyzeTrend(mediumDataset);
        var movingAvg = analyzer.calculateMovingAverage(mediumDataset, 5);
        blackhole.consume(trend);
        blackhole.consume(movingAvg);
    }

    @Benchmark
    @Threads(4)
    public void concurrentAnalysis(Blackhole blackhole) {
        blackhole.consume(analyzer.analyzeTrend(mediumDataset));
    }

    private List<DataPoint> generateDataset(int size, double startValue, double factor) {
        List<DataPoint> points = new ArrayList<>(size);
        LocalDateTime baseTime = LocalDateTime.now();
        double currentValue = startValue;

        for (int i = 0; i < size; i++) {
            points.add(new DataPoint(baseTime.plusMinutes(i), currentValue, 0.0));
            currentValue *= factor;
        }

        return points;
    }

    private List<DataPoint> generateVolatileDataset(int size) {
        List<DataPoint> points = new ArrayList<>(size);
        LocalDateTime baseTime = LocalDateTime.now();
        double baseValue = 100.0;

        for (int i = 0; i < size; i++) {
            double volatility = Math.sin(i * 0.5) * 50;
            points.add(new DataPoint(
                baseTime.plusMinutes(i),
                baseValue + volatility,
                0.0
            ));
        }

        return points;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(PerformanceTrendAnalyzerBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(3)
            .measurementIterations(5)
            .build();

        new Runner(opt).run();
    }
}
