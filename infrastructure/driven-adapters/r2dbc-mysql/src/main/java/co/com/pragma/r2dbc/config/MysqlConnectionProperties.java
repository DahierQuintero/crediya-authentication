package co.com.pragma.r2dbc.config;

// TODO: Load properties from the application.yaml file or from secrets manager
// import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.r2dbc")
public record MysqlConnectionProperties(
        String host,
        Integer port,
        String database,
        String username,
        String password) {
}
