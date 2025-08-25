package co.com.pragma.r2dbc.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.asyncer.r2dbc.mysql.constant.SslMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MysqlConnectionPool {
    /* Change these values for your project */
    public static final int INITIAL_SIZE = 12;
    public static final int MAX_SIZE = 15;
    public static final int MAX_IDLE_TIME = 30;
    public static final int DEFAULT_PORT = 3306;

    /**
     * Exposed for testing: builds the MySQL R2DBC connection configuration from properties.
     */
    public MySqlConnectionConfiguration getConnectionConfig(MysqlConnectionProperties properties) {
        return MySqlConnectionConfiguration.builder()
                .host(properties.host())
                .port(properties.port() != null ? properties.port() : DEFAULT_PORT)
                .database(properties.database())
                .username(properties.username())
                .password(properties.password())
                .sslMode(SslMode.DISABLED)
                //.connectTimeout(Duration.ofSeconds(5))               // opcional: timeout de conexión
                // .useServerPrepareStatement()                         // opcional: prepared stmts del lado servidor
                //.createDatabaseIfNotExist()                          // opcional: crea BD si no existe// Cambia a REQUIRED si usas TLS
                .build();
    }

    @Bean
    public ConnectionPool connectionPool(MysqlConnectionProperties properties) {
        // Builder de configuración del driver R2DBC para MySQL
        MySqlConnectionConfiguration configuration = getConnectionConfig(properties);

        // ConnectionFactory propio del driver MySQL R2DBC
        ConnectionFactory connectionFactory = MySqlConnectionFactory.from(configuration);

        // Configuración del pool (io.r2dbc.pool)
        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
                .name("api-mysql-connection-pool")
                .initialSize(INITIAL_SIZE)
                .maxSize(MAX_SIZE)
                .maxIdleTime(Duration.ofMinutes(MAX_IDLE_TIME))
                .validationQuery("SELECT 1")
                .build();

        return new ConnectionPool(poolConfiguration);
    }
}