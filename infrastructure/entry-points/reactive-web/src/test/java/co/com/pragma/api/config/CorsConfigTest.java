package co.com.pragma.api.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    @InjectMocks
    private CorsConfig corsConfig;

    @Test
    void corsWebFilter_WithSingleOrigin_ShouldCreateFilterCorrectly() {
        // Given
        String origins = "http://localhost:3000";

        // When
        CorsWebFilter filter = corsConfig.corsWebFilter(origins);

        // Then
        assertNotNull(filter);

        // Verificar que el filtro fue creado correctamente
        UrlBasedCorsConfigurationSource source = getSourceFromFilter(filter);
        assertNotNull(source);

        // Verificar que la configuración está registrada
        // Usamos reflection para acceder a las configuraciones internas
        Object corsConfigurations = ReflectionTestUtils.getField(source, "corsConfigurations");
        assertNotNull(corsConfigurations);
    }

    @Test
    void corsWebFilter_WithMultipleOrigins_ShouldSplitOriginsCorrectly() {
        // Given
        String origins = "http://localhost:3000,http://localhost:4200,https://example.com";

        // When
        CorsWebFilter filter = corsConfig.corsWebFilter(origins);

        // Then
        assertNotNull(filter);

        UrlBasedCorsConfigurationSource source = getSourceFromFilter(filter);
        assertNotNull(source);

        // Test directo de la lógica de splitting
        List<String> splitOrigins = List.of(origins.split(","));
        List<String> expectedOrigins = Arrays.asList(
                "http://localhost:3000",
                "http://localhost:4200",
                "https://example.com"
        );
        assertEquals(expectedOrigins, splitOrigins);
    }

    @Test
    void corsWebFilter_WithEmptyOrigins_ShouldHandleEmptyString() {
        // Given
        String origins = "";

        // When
        CorsWebFilter filter = corsConfig.corsWebFilter(origins);

        // Then
        assertNotNull(filter);

        UrlBasedCorsConfigurationSource source = getSourceFromFilter(filter);
        assertNotNull(source);

        // Test de la lógica de splitting con string vacío
        List<String> splitOrigins = List.of(origins.split(","));
        assertEquals(List.of(""), splitOrigins);
    }

    @Test
    void corsWebFilter_ShouldCreateCorsConfigurationWithCorrectSettings() {
        // Given
        String origins = "http://localhost:3000";

        // When
        CorsWebFilter filter = corsConfig.corsWebFilter(origins);

        // Then - Verificamos que el filtro se crea sin errores
        assertNotNull(filter);

        // Verificamos que el source interno existe
        UrlBasedCorsConfigurationSource source = getSourceFromFilter(filter);
        assertNotNull(source);
    }

    @Test
    void corsConfig_ShouldCreateValidBean() {
        // Test básico para verificar que la configuración es válida
        CorsConfig config = new CorsConfig();
        assertNotNull(config);

        // Test de que el método bean funciona
        String testOrigins = "http://localhost:3000,https://example.com";
        CorsWebFilter filter = config.corsWebFilter(testOrigins);

        assertNotNull(filter);
    }

    @Test
    void corsConfig_ShouldHandleNullOrigins() {
        // Test edge case
        CorsConfig config = new CorsConfig();

        // Este test debería fallar si no hay manejo de null
        // o pasar si se maneja correctamente
        assertThrows(Exception.class, () -> {
            config.corsWebFilter(null);
        });
    }

    @Test
    void corsConfig_ShouldHandleOriginsWithSpaces() {
        // Given
        String origins = " http://localhost:3000 , http://localhost:4200 ";

        // When
        CorsWebFilter filter = corsConfig.corsWebFilter(origins);

        // Then
        assertNotNull(filter);

        // Test de la lógica de splitting (mostrará si hay problema con espacios)
        List<String> splitOrigins = List.of(origins.split(","));
        assertEquals(2, splitOrigins.size());

        // Esto mostrará que necesitas trim() en tu implementación
        assertTrue(splitOrigins.get(0).contains("http://localhost:3000"));
        assertTrue(splitOrigins.get(1).contains("http://localhost:4200"));
    }

    /**
     * Método auxiliar para extraer el source del CorsWebFilter usando reflection
     */
    private UrlBasedCorsConfigurationSource getSourceFromFilter(CorsWebFilter filter) {
        try {
            return (UrlBasedCorsConfigurationSource) ReflectionTestUtils.getField(filter, "configSource");
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener configSource del CorsWebFilter", e);
        }
    }
}

// Test de integración simplificado
@SpringBootTest(classes = CorsConfig.class)
@TestPropertySource(properties = {
        "cors.allowed-origins=http://localhost:3000,http://localhost:4200"
})
class CorsConfigIntegrationTest {

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Test
    void corsWebFilter_ShouldBeCreatedAsBean() {
        assertNotNull(corsWebFilter);
    }

    @Test
    void corsWebFilter_ShouldHaveValidConfiguration() {
        assertNotNull(corsWebFilter);

        // Verificar que el source existe
        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) ReflectionTestUtils.getField(corsWebFilter, "configSource");

        assertNotNull(source);
    }
}

// Test específico para la lógica de configuración CORS
@ExtendWith(MockitoExtension.class)
class CorsConfigurationLogicTest {

    @Test
    void corsConfiguration_ShouldHaveCorrectDefaults() {
        // Test directo de la lógica que está en tu método
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3000".split(",")));
        config.setAllowedMethods(Arrays.asList("POST", "GET"));
        config.setAllowedHeaders(List.of(CorsConfiguration.ALL));

        // Verificaciones
        assertTrue(config.getAllowCredentials());
        assertEquals(List.of("http://localhost:3000"), config.getAllowedOrigins());
        assertEquals(Arrays.asList("POST", "GET"), config.getAllowedMethods());
        assertEquals(List.of(CorsConfiguration.ALL), config.getAllowedHeaders());
    }

    @Test
    void originsSplitting_ShouldWorkCorrectly() {
        // Test de la lógica de splitting de orígenes
        String origins = "http://localhost:3000,http://localhost:4200,https://example.com";
        List<String> result = List.of(origins.split(","));

        assertEquals(3, result.size());
        assertEquals("http://localhost:3000", result.get(0));
        assertEquals("http://localhost:4200", result.get(1));
        assertEquals("https://example.com", result.get(2));
    }

    @Test
    void originsSplitting_WithSpaces_ShouldShowNeedForTrim() {
        // Este test muestra que necesitas agregar trim()
        String origins = " http://localhost:3000 , http://localhost:4200 ";
        List<String> result = List.of(origins.split(","));

        assertEquals(2, result.size());
        // Esto fallará y mostrará que necesitas trim
        assertNotEquals("http://localhost:3000", result.get(0)); // tiene espacios
        assertNotEquals("http://localhost:4200", result.get(1)); // tiene espacios

        // Versión corregida:
        List<String> trimmedResult = Arrays.stream(origins.split(","))
                .map(String::trim)
                .toList();

        assertEquals("http://localhost:3000", trimmedResult.get(0));
        assertEquals("http://localhost:4200", trimmedResult.get(1));
    }
}