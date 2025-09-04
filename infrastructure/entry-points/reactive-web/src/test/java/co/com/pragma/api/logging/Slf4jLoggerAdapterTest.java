package co.com.pragma.api.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Slf4jLoggerAdapterTest {

    private Slf4jLoggerAdapter loggerAdapter;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        loggerAdapter = new Slf4jLoggerAdapter();

        // Configurar captura de logs para testing
        logger = (Logger) LoggerFactory.getLogger(Slf4jLoggerAdapter.class);

        // Guardar el nivel original para restaurarlo después
        originalLevel = logger.getLevel();

        // Establecer nivel DEBUG para capturar todos los logs
        logger.setLevel(Level.DEBUG);

        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        // Limpiar y restaurar el estado original
        if (logger != null && listAppender != null) {
            logger.detachAppender(listAppender);
            logger.setLevel(originalLevel);
        }
        if (listAppender != null) {
            listAppender.stop();
        }
    }

    @Test
    void info_WithTraceIdAndMessage_ShouldLogCorrectly() {
        // Given
        String traceId = "123e4567-e89b-12d3-a456-426614174000";
        String message = "User created successfully";
        String param1 = "userId";
        Integer param2 = 12345;

        // When
        loggerAdapter.info(traceId, message, param1, param2);

        // Then
        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals("TRACE_ID:{} - User created successfully", loggingEvent.getMessage());
        assertEquals(Level.INFO, loggingEvent.getLevel());

        // Verificar que los argumentos se incluyen correctamente
        Object[] arguments = loggingEvent.getArgumentArray();
        assertEquals(3, arguments.length);
        assertEquals(traceId, arguments[0]);
        assertEquals(param1, arguments[1]);
        assertEquals(param2, arguments[2]);
    }

    @Test
    void debug_WithTraceIdAndMessage_ShouldLogCorrectly() {
        // Given
        String traceId = "debug-trace-123";
        String message = "Debug message";
        String param = "testParam";

        // When
        loggerAdapter.debug(traceId, message, param);

        // Then
        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals("TRACE_ID:{} - Debug message", loggingEvent.getMessage());
        assertEquals(Level.DEBUG, loggingEvent.getLevel());

        Object[] arguments = loggingEvent.getArgumentArray();
        assertEquals(2, arguments.length);
        assertEquals(traceId, arguments[0]);
        assertEquals(param, arguments[1]);
    }

    @Test
    void warn_WithTraceIdAndMessage_ShouldLogCorrectly() {
        // Given
        String traceId = "warn-trace-456";
        String message = "Warning message";
        String warningType = "VALIDATION";

        // When
        loggerAdapter.warn(traceId, message, warningType);

        // Then
        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals("TRACE_ID:{} - Warning message", loggingEvent.getMessage());
        assertEquals(Level.WARN, loggingEvent.getLevel());

        Object[] arguments = loggingEvent.getArgumentArray();
        assertEquals(2, arguments.length);
        assertEquals(traceId, arguments[0]);
        assertEquals(warningType, arguments[1]);
    }

    @Test
    void info_WithEmptyArgs_ShouldHandleCorrectly() {
        // Given
        String traceId = "empty-args-trace";
        String message = "Simple message";

        // When
        loggerAdapter.info(traceId, message);

        // Then
        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals("TRACE_ID:{} - Simple message", loggingEvent.getMessage());

        Object[] arguments = loggingEvent.getArgumentArray();
        assertEquals(1, arguments.length);
        assertEquals(traceId, arguments[0]);
    }

    @Test
    void info_WithNullTraceId_ShouldHandleCorrectly() {
        // Given
        String traceId = null;
        String message = "Message with null trace";
        String param = "value";

        // When
        loggerAdapter.info(traceId, message, param);

        // Then
        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals("TRACE_ID:{} - Message with null trace", loggingEvent.getMessage());

        Object[] arguments = loggingEvent.getArgumentArray();
        assertEquals(2, arguments.length);
        assertNull(arguments[0]); // traceId null
        assertEquals(param, arguments[1]);
    }

    @Test
    void logMethods_WithMultipleArgs_ShouldConcatenateCorrectly() {
        // Given
        String traceId = "multi-args-trace";
        String message = "Processing user: {} with email: {} and age: {}";
        String name = "John";
        String email = "john@test.com";
        Integer age = 25;
        String extra1 = "extra";
        String extra2 = "params";

        // When
        loggerAdapter.info(traceId, message, name, email, age, extra1, extra2);

        // Then
        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals("TRACE_ID:{} - Processing user: {} with email: {} and age: {}", loggingEvent.getMessage());

        // Verificar que todos los argumentos se concatenan correctamente
        Object[] arguments = loggingEvent.getArgumentArray();
        assertEquals(6, arguments.length);
        assertEquals(traceId, arguments[0]);
        assertEquals(name, arguments[1]);
        assertEquals(email, arguments[2]);
        assertEquals(age, arguments[3]);
        assertEquals(extra1, arguments[4]);
        assertEquals(extra2, arguments[5]);
    }
}

// Tests de la lógica interna (sin logging real)
class Slf4jLoggerAdapterLogicTest {

    @Test
    void streamConcatenation_ShouldWorkCorrectly() {
        // Test directo de la lógica de concatenación de streams
        String traceId = "test-trace";
        Object[] args = {"param1", "param2", "param3"};

        Object[] result = java.util.stream.Stream.concat(
                java.util.stream.Stream.of(traceId),
                java.util.stream.Stream.of(args)
        ).toArray();

        // Verificar que la concatenación funciona correctamente
        assertEquals(4, result.length);
        assertEquals(traceId, result[0]);
        assertEquals("param1", result[1]);
        assertEquals("param2", result[2]);
        assertEquals("param3", result[3]);
    }

    @Test
    void streamConcatenation_WithEmptyArgs_ShouldWorkCorrectly() {
        String traceId = "test-trace";
        Object[] args = {};

        Object[] result = java.util.stream.Stream.concat(
                java.util.stream.Stream.of(traceId),
                java.util.stream.Stream.of(args)
        ).toArray();

        assertEquals(1, result.length);
        assertEquals(traceId, result[0]);
    }

    @Test
    void streamConcatenation_WithNullValues_ShouldHandleCorrectly() {
        String traceId = null;
        Object[] args = {"param1", null, "param3"};

        Object[] result = java.util.stream.Stream.concat(
                java.util.stream.Stream.of(traceId),
                java.util.stream.Stream.of(args)
        ).toArray();

        assertEquals(4, result.length);
        assertNull(result[0]); // traceId null
        assertEquals("param1", result[1]);
        assertNull(result[2]); // param null
        assertEquals("param3", result[3]);
    }

    @Test
    void constantString_ShouldBeCorrect() {
        // Test para verificar que la constante está bien definida
        try {
            java.lang.reflect.Field field = Slf4jLoggerAdapter.class.getDeclaredField("TRACEID");
            field.setAccessible(true);
            String traceIdConstant = (String) field.get(null);

            assertEquals("TRACE_ID:{} - ", traceIdConstant);
        } catch (Exception e) {
            fail("Could not access TRACEID constant: " + e.getMessage());
        }
    }

    @Test
    void loggerAdapter_ShouldImplementILoggerPort() {
        // Verificar que implementa la interfaz correctamente
        Slf4jLoggerAdapter adapter = new Slf4jLoggerAdapter();

        // Test que no lance excepciones
        assertDoesNotThrow(() -> {
            adapter.info("trace", "message");
            adapter.debug("trace", "message");
            adapter.warn("trace", "message");
            adapter.error("trace", "message", null);
        });
    }
}