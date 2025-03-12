package vn.com.fecredit.app.monitoring;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Targeted failure injection for chaos testing
 */
public class FailureInjector {
    private static final Random random = new Random();
    private final AtomicInteger injectedFailures = new AtomicInteger(0);
    private final double failureRate;
    
    public FailureInjector(double failureRate) {
        if (failureRate < 0.0 || failureRate > 1.0) {
            throw new IllegalArgumentException("Failure rate must be between 0.0 and 1.0");
        }
        this.failureRate = failureRate;
    }

    /**
     * Execute with potential failure injection
     */
    public <T> T executeWithFailures(Supplier<T> operation) throws Throwable {
        if (shouldInjectFailure()) {
            injectedFailures.incrementAndGet();
            throw selectRandomException();
        }
        return operation.get();
    }

    /**
     * Execute with potential failure injection for void operations
     */
    public void executeWithFailures(Runnable operation) throws Throwable {
        if (shouldInjectFailure()) {
            injectedFailures.incrementAndGet();
            throw selectRandomException();
        }
        operation.run();
    }

    /**
     * Corrupts file content if it exists
     */
    public void corruptFile(Path file) throws IOException {
        if (Files.exists(file)) {
            byte[] content = Files.readAllBytes(file);
            // Corrupt random bytes
            for (int i = 0; i < content.length / 10; i++) {
                int position = random.nextInt(content.length);
                content[position] = (byte) random.nextInt(256);
            }
            Files.write(file, content, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    /**
     * Simulates various system-level failures
     */
    public void injectSystemFailure() throws Throwable {
        switch (random.nextInt(5)) {
            case 0: // Memory pressure
                byte[] leak = new byte[100 * 1024 * 1024]; // 100MB
                Thread.sleep(100);
                leak = null;
                System.gc();
                break;
            case 1: // CPU pressure
                long end = System.currentTimeMillis() + 1000;
                while (System.currentTimeMillis() < end) {
                    Math.pow(random.nextDouble(), random.nextDouble());
                }
                break;
            case 2: // Thread interruption
                Thread.currentThread().interrupt();
                break;
            case 3: // File system pressure
                Path tempFile = Files.createTempFile("chaos", ".tmp");
                Files.write(tempFile, new byte[10 * 1024 * 1024]); // 10MB
                Files.delete(tempFile);
                break;
            case 4: // Thread sleep
                Thread.sleep(random.nextInt(1000));
                break;
        }
    }

    /**
     * Corrupts in-memory data structures
     */
    public void corruptData(Object data) {
        if (data instanceof byte[]) {
            byte[] bytes = (byte[]) data;
            if (bytes.length > 0) {
                bytes[random.nextInt(bytes.length)] = (byte) random.nextInt(256);
            }
        } else if (data instanceof String) {
            // No direct mutation as Strings are immutable
            throw new UnsupportedOperationException("Cannot corrupt immutable String");
        }
    }

    private boolean shouldInjectFailure() {
        return random.nextDouble() < failureRate;
    }

    private Throwable selectRandomException() {
        switch (random.nextInt(7)) {
            case 0:
                return new OutOfMemoryError("Simulated OOM");
            case 1:
                return new IOException("Simulated IO failure");
            case 2:
                return new IllegalStateException("Simulated state corruption");
            case 3:
                return new SecurityException("Simulated security violation");
            case 4:
                return new RuntimeException("Simulated runtime error");
            case 5:
                return new InterruptedException("Simulated interrupt");
            default:
                return new Exception("Simulated general failure");
        }
    }

    /**
     * Creates network-related failures
     */
    public void injectNetworkFailure() throws IOException, InterruptedException {
        switch (random.nextInt(3)) {
            case 0: // Latency
                Thread.sleep(random.nextInt(2000));
                break;
            case 1: // Connection reset
                throw new IOException("Connection reset");
            case 2: // Timeout
                throw new IOException("Connection timed out");
        }
    }

    /**
     * Simulates resource exhaustion
     */
    public void exhaustResources() throws Throwable {
        switch (random.nextInt(3)) {
            case 0: // File handles
                List<Path> tempFiles = new ArrayList<>();
                try {
                    for (int i = 0; i < 100; i++) {
                        Path temp = Files.createTempFile("exhaustion", ".tmp");
                        Files.newOutputStream(temp);
                        tempFiles.add(temp);
                    }
                } finally {
                    tempFiles.forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {}
                    });
                }
                break;

            case 1: // Memory
                List<byte[]> leaks = new ArrayList<>();
                try {
                    for (int i = 0; i < 100; i++) {
                        leaks.add(new byte[1024 * 1024]); // 1MB chunks
                    }
                } finally {
                    leaks.clear();
                    System.gc();
                }
                break;

            case 2: // Threads
                List<Thread> threads = new ArrayList<>();
                try {
                    for (int i = 0; i < 100; i++) {
                        Thread t = new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                        t.start();
                        threads.add(t);
                    }
                } finally {
                    threads.forEach(Thread::interrupt);
                    for (Thread t : threads) {
                        try {
                            t.join(100);
                        } catch (InterruptedException ignored) {}
                    }
                }
                break;
        }
    }

    public int getInjectedFailures() {
        return injectedFailures.get();
    }
}
