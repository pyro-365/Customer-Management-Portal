package com.customer.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String dbUrl = env.getProperty("SUPABASE_DB_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = env.getProperty("spring.datasource.url");
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");

        if (dbUrl != null && (dbUrl.startsWith("postgresql://") || dbUrl.startsWith("postgres://"))) {
            try {
                URI uri = new URI(dbUrl);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath() != null && !uri.getPath().isEmpty() ? uri.getPath() : "/postgres";
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                String query = uri.getQuery();
                if (query != null && !query.isBlank()) {
                    if (!query.contains("sslmode")) {
                        query += "&sslmode=require";
                    }
                    jdbcUrl += "?" + query;
                } else {
                    jdbcUrl += "?sslmode=require";
                }
                config.setJdbcUrl(jdbcUrl);
                config.addDataSourceProperty("sslmode", "require");

                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    config.setUsername(URLDecoder.decode(parts[0], StandardCharsets.UTF_8));
                    config.setPassword(URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                if (!dbUrl.startsWith("jdbc:")) {
                    dbUrl = "jdbc:" + dbUrl;
                }
                config.setJdbcUrl(dbUrl);
            }
        } else {
            if (dbUrl != null && !dbUrl.startsWith("jdbc:")) {
                dbUrl = "jdbc:" + dbUrl;
            }
            config.setJdbcUrl(dbUrl != null ? dbUrl : "jdbc:postgresql://localhost:5432/postgres");
            config.setUsername(env.getProperty("spring.datasource.username", "postgres"));
            config.setPassword(env.getProperty("spring.datasource.password", ""));
        }

        // Connection pool defaults optimized for Supabase transaction/session poolers
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(60000);
        config.setConnectionTimeout(20000);

        return new HikariDataSource(config);
    }
}
