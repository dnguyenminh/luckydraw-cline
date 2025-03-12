package vn.com.fecredit.app.performance.trend;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for DataPoint operations.
 * Run with: mvn clean install && java -jar target/benchmarks.jar
 */
@State(Scope.Thread)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class DataPointBenchmark {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2024, 1, 1, 0, 0);
    private DataPoint point;
    private List<DataPoint> points;
    private static final int LIST_SIZE = 10_000;

    @Setup
    public void setup() {
        point = new DataPoint(BASE_TIME, 100.0, 5.0);
        points = new ArrayList<>(LIST_SIZE);
        for (int i = 0; i < LIST_SIZE; i++) {
            points.add(new DataPoint(
                BASE_TIME.plusSeconds(i),
                Math.sin(i * 0.1) * 100,
                Math.random() * 5
            ));
        }
    }

    @Benchmark
    public void construction(Blackhole blackhole) {
        blackhole.consume(new DataPoint(BASE_TIME, 100.0, 5.0));
    }

    @Benchmark
    public void factoryMethod(Blackhole blackhole) {
        blackhole.consume(DataPoint.of(BASE_TIME, 100.0));
    }

    @Benchmark
    public void currentTimeConstruction(Blackhole blackhole) {
        blackhole.consume(DataPoint.now(100.0));
    }

    @Benchmark
    public void comparison(Blackhole blackhole) {
        DataPoint other = new DataPoint(BASE_TIME.plusSeconds(1), 110.0, 5.0);
        blackhole.consume(point.compareTo(other));
    }

    @Benchmark
    public void stableCheck(Blackhole blackhole) {
        blackhole.consume(point.isStable());
    }

    @Benchmark
    public void changeCalculations(Blackhole blackhole) {
        blackhole.consume(point.relativeChange(90.0));
        blackhole.consume(point.absoluteChange(90.0));
    }

    @Benchmark
    public void modification(Blackhole blackhole) {
        blackhole.consume(point.withValue(110.0));
        blackhole.consume(point.withDeviation(6.0));
    }

    @Benchmark
    @OperationsPerInvocation(LIST_SIZE)
    public void sorting(Blackhole blackhole) {
        List<DataPoint> copy = new ArrayList<>(points);
        Collections.sort(copy);
        blackhole.consume(copy);
    }

    @Benchmark
    @OperationsPerInvocation(LIST_SIZE)
    public void filtering(Blackhole blackhole) {
        points.stream()
            .filter(DataPoint::isStable)
            .forEach(blackhole::consume);
    }

    @Benchmark
    @OperationsPerInvocation(LIST_SIZE)
    public void transformation(Blackhole blackhole) {
        points.stream()
            .map(p -> p.withValue(p.value() * 1.1))
            .forEach(blackhole::consume);
    }

    @Benchmark
    public void timeDifference(Blackhole blackhole) {
        for (int i = 1; i < points.size(); i++) {
            blackhole.consume(points.get(i).secondsFrom(points.get(i-1)));
        }
    }

    @Benchmark
    @OperationsPerInvocation(LIST_SIZE)
    public void statisticalOperations(Blackhole blackhole) {
        double sum = 0.0;
        double maxDev = 0.0;
        for (DataPoint p : points) {
            sum += p.value();
            maxDev = Math.max(maxDev, p.deviation());
        }
        blackhole.consume(sum / points.size());
        blackhole.consume(maxDev);
    }

    @Benchmark
    @GroupThreads(4)
    @Group("concurrent")
    public void concurrentReads(Blackhole blackhole) {
        points.forEach(p -> {
            blackhole.consume(p.value());
            blackhole.consume(p.deviation());
            blackhole.consume(p.timestamp());
        });
    }

    @Benchmark
    @GroupThreads(2)
    @Group("concurrent")
    public void concurrentTransforms(Blackhole blackhole) {
        points.stream()
            .map(p -> p.withValue(p.value() * 1.1))
            .forEach(blackhole::consume);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(DataPointBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(3)
            .measurementIterations(5)
            .build();

        new Runner(opt).run();
    }
}
