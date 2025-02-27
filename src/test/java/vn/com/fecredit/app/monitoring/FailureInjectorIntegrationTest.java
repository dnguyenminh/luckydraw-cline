package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;

import vn.com.fecredit.app.monitoring.base.*;

/**
 * Integration tests for FailureInjector with real system resources
 */
@TestCategory.IntegrationTest
@TestCategory.ChaosTest
@DisplayName("Failure Injector Integration Tests")
class FailureInjectorIntegrationTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = Logger.getLogger(FailureInjectorIntegrationTest.class.getName());
    private static final String OK_RESPONSE = "OK";
    private static final int RESPONSE_DELAY_MS = 100;
    private static final int ACCEPT_TIMEOUT_MS = 1000;
    private static final int SOCKET_READ_BUFFER_SIZE = 2;
    
    private FailureInjector injector;
    private ServerSocket serverSocket;
    private volatile boolean serverRunning;
    private final List<Future<?>> pendingTasks = new ArrayList<>();

    /**
     * Exception wrapper for network operations
     */
    private static class NetworkException extends RuntimeException {
        NetworkException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Handles socket connections with proper resource management
     */
    private static class ConnectionHandler {
        private final String host;
        private final int port;
        private final int timeout;
        private final Logger log;

        ConnectionHandler(String host, int port, int timeout) {
            this.host = host;
            this.port = port;
            this.timeout = timeout;
            this.log = Logger.getLogger(ConnectionHandler.class.getName());
        }

        String connect() {
            try {
                Socket socket = new Socket(host, port);
                socket.setSoTimeout(timeout);
                try (socket) {
                    return readResponse(socket);
                }
            } catch (IOException e) {
                throw new NetworkException("Connection failed", e);
            }
        }

        private String readResponse(Socket socket) {
            try (InputStream in = socket.getInputStream()) {
                byte[] response = new byte[SOCKET_READ_BUFFER_SIZE];
                int read = in.read(response);
                if (read <= 0) {
                    throw new NetworkException("No data received", null);
                }
                return new String(response, 0, read);
            } catch (IOException e) {
                throw new NetworkException("Failed to read response", e);
            }
        }
    }

    /**
     * Handles server-side socket operations
     */
    private static class TestServer implements AutoCloseable {
        private final ServerSocket server;
        private final ExecutorService executor;
        private final Logger log;
        private volatile boolean running;

        TestServer(ServerSocket server, ExecutorService executor) {
            this.server = server;
            this.executor = executor;
            this.log = Logger.getLogger(TestServer.class.getName());
            this.running = true;
        }

        Future<?> submitClient(Socket client) {
            return executor.submit(() -> {
                if (client != null) {
                    try (client) {
                        handleClient(client);
                    } catch (Exception e) {
                        log.warning(() -> String.format("Error handling client: %s", e.getMessage()));
                    }
                }
            });
        }

        private void handleClient(Socket client) {
            try {
                try (OutputStream out = client.getOutputStream()) {
                    Thread.sleep(RESPONSE_DELAY_MS); // NOSONAR - test code
                    out.write(OK_RESPONSE.getBytes());
                    out.flush();
                }
            } catch (Exception e) {
                throw new NetworkException("Failed to handle client", e);
            }
        }

        Socket acceptClient() {
            if (!running) {
                throw new NetworkException("Server is shutting down", null);
            }
            try {
                Socket client = server.accept();
                if (client != null) {
                    client.setSoTimeout(ACCEPT_TIMEOUT_MS);
                }
                return client;
            } catch (IOException e) {
                if (!(e instanceof SocketTimeoutException)) {
                    log.warning(() -> String.format("Accept failed: %s", e.getMessage()));
                }
                throw new NetworkException("Failed to accept client", e);
            }
        }

        void shutdown() {
            running = false;
        }

        @Override
        public void close() {
            shutdown();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        injector = new FailureInjector(0.3); // 30% failure rate
        
        try {
            serverSocket = createServerSocket();
            serverRunning = true;
            startTestServer();
            LOGGER.info(() -> String.format("Test server started on port %d", serverSocket.getLocalPort()));
        } catch (IOException e) {
            LOGGER.severe(() -> String.format("Failed to start test server: %s", e.getMessage()));
            throw e;
        }
    }

    private ServerSocket createServerSocket() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        socket.setSoTimeout(ACCEPT_TIMEOUT_MS);
        return socket;
    }

    private void startTestServer() {
        TestServer server = new TestServer(serverSocket, executor);
        
        Future<?> serverTask = executor.submit(() -> {
            try (server) {
                while (serverRunning) {
                    try {
                        Socket client = server.acceptClient();
                        if (client != null) {
                            pendingTasks.add(server.submitClient(client));
                        }
                    } catch (NetworkException e) {
                        if (serverRunning && !(e.getCause() instanceof SocketTimeoutException)) {
                            LOGGER.warning(() -> String.format("Server accept error: %s", e.getMessage()));
                        }
                        if (!serverRunning) {
                            break;
                        }
                    }
                }
            }
            return null;
        });
        pendingTasks.add(serverTask);
    }

    @Test
    @Order(1)
    @DisplayName("File System Operations Under Chaos")
    void testFileSystemOperations() throws IOException, InterruptedException {
        Path testDir = tempDir.resolve("chaos-test");
        List<Path> testFiles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Path file = Files.createTempFile(testDir, "chaos-", ".txt");
            Files.writeString(file, "test content " + i);
            testFiles.add(file);
        }
        LOGGER.info(() -> String.format("Created %d test files in %s", testFiles.size(), testDir));

        Map<String, Integer> results = new ConcurrentHashMap<>();
        results.put("success", 0);
        results.put("failure", 0);

        LoadTestConfig config = new LoadTestConfig(
            5, 
            Duration.ofSeconds(30),
            Duration.ofMillis(100)
        );

        LOGGER.info("Starting file system chaos test...");
        runLoadTest(threadId -> {
            try {
                Path file = testFiles.get(threadId % testFiles.size());
                injector.executeWithFailures(() -> {
                    injector.corruptFile(file);
                    return Files.readString(file);
                });
                results.compute("success", (k, v) -> v + 1);
            } catch (Throwable e) {
                results.compute("failure", (k, v) -> v + 1);
                LOGGER.fine(() -> String.format("Induced failure: %s", e.getMessage()));
            }
        }, config);

        assertTrue(results.get("success") > 0, "Should have some successful operations");
        assertTrue(results.get("failure") > 0, "Should have some failed operations");
        LOGGER.info(() -> String.format("File operations completed - Success: %d, Failures: %d", 
            results.get("success"), results.get("failure")));
    }

    @Test
    @Order(2)
    @DisplayName("Network Operations Under Chaos")
    void testNetworkOperations() {
        ConnectionHandler handler = new ConnectionHandler("localhost", serverSocket.getLocalPort(), ACCEPT_TIMEOUT_MS);
        Map<String, Integer> results = new ConcurrentHashMap<>();
        results.put("success", 0);
        results.put("failure", 0);

        LoadTestConfig config = new LoadTestConfig(
            10,
            Duration.ofSeconds(30),
            Duration.ofMillis(50)
        );

        LOGGER.info("Starting network chaos test...");
        runLoadTest(threadId -> {
            try {
                String result = injector.executeWithFailures(() -> {
                    injector.injectNetworkFailure();
                    return handler.connect();
                });
                
                if (OK_RESPONSE.equals(result)) {
                    results.compute("success", (k, v) -> v + 1);
                }
            } catch (Throwable e) {
                results.compute("failure", (k, v) -> v + 1);
                LOGGER.fine(() -> String.format("Induced network failure: %s", e.getMessage()));
            }
        }, config);

        assertTrue(results.get("success") > 0, "Should have some successful connections");
        assertTrue(results.get("failure") > 0, "Should have some failed connections");
        LOGGER.info(() -> String.format("Network operations completed - Success: %d, Failures: %d",
            results.get("success"), results.get("failure")));
    }

    @Test
    @Order(3)
    @DisplayName("Resource Exhaustion Recovery")
    void testResourceExhaustion() throws InterruptedException {
        List<Exception> exceptions = new ArrayList<>();
        Map<String, Number> beforeMetrics = collectTestMetrics();
        LOGGER.info(() -> String.format("Initial metrics: %s", beforeMetrics));

        runWithRetry(() -> {
            try {
                injector.exhaustResources();
            } catch (Throwable e) {
                exceptions.add(new Exception("Resource exhaustion failed", e));
                LOGGER.warning(() -> String.format("Resource exhaustion attempt failed: %s", e.getMessage()));
            }
            return null;
        }, 3, Duration.ofSeconds(1));

        System.gc();
        Thread.sleep(1000); // NOSONAR - test code

        Map<String, Number> afterMetrics = collectTestMetrics();
        LOGGER.info(() -> String.format("Post-recovery metrics: %s", afterMetrics));
        
        assertTrue(afterMetrics.get("memoryUsed").longValue() < beforeMetrics.get("memoryUsed").longValue(),
            "Memory should be released after GC");
        
        if (!exceptions.isEmpty()) {
            exceptions.forEach(e -> 
                LOGGER.warning(() -> String.format("Resource exhaustion exception: %s - %s",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "null")));
        }
    }

    @AfterEach
    void cleanup() {
        serverRunning = false;
        
        pendingTasks.forEach(task -> task.cancel(true));
        pendingTasks.clear();
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                LOGGER.info("Test server shut down");
            } catch (IOException e) {
                LOGGER.warning(() -> String.format("Error closing server socket: %s", e.getMessage()));
            }
        }
    }
}
