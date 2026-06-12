package com.example.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbProvider = System.getenv("DB_PROVIDER");
        if (dbProvider != null) {
            dbProvider = dbProvider.trim().toUpperCase();
        }

        String databaseUrl = System.getenv("DATABASE_URL");
        String pgHost = System.getenv("PGHOST");
        String mysqlHost = System.getenv("MYSQLHOST");
        String dbHost = System.getenv("DB_HOST");

        String url = null;
        String username = null;
        String password = null;
        String driverClassName = null;

        System.out.println("DataSourceConfig: DB_PROVIDER is: " + dbProvider);

        if ("H2".equals(dbProvider)) {
            System.out.println("DataSourceConfig: Forcing H2 Database connection.");
        } else if ("POSTGRES".equals(dbProvider) || "POSTGRESQL".equals(dbProvider)) {
            System.out.println("DataSourceConfig: Forcing PostgreSQL database selection.");
            DatabaseConnectionDetails details = parseDatabaseUrl(databaseUrl);
            if (details != null && details.url.contains("postgresql")) {
                url = details.url;
                username = details.username;
                password = details.password;
                driverClassName = details.driverClassName;
            }
            if (url == null && pgHost != null && !pgHost.trim().isEmpty()) {
                String pgPort = System.getenv("PGPORT") != null ? System.getenv("PGPORT") : "5432";
                String pgDb = System.getenv("PGDATABASE") != null ? System.getenv("PGDATABASE") : "railway";
                url = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDb;
                username = System.getenv("PGUSER") != null ? System.getenv("PGUSER") : "postgres";
                password = System.getenv("PGPASSWORD");
                driverClassName = "org.postgresql.Driver";
            }
            if (url == null && dbHost != null && !dbHost.trim().isEmpty()) {
                String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
                String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "ecommerce";
                url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
                username = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
                password = System.getenv("DB_PASSWORD");
                driverClassName = "org.postgresql.Driver";
            }
        } else if ("MYSQL".equals(dbProvider)) {
            System.out.println("DataSourceConfig: Forcing MySQL database selection.");
            DatabaseConnectionDetails details = parseDatabaseUrl(databaseUrl);
            if (details != null && details.url.contains("mysql")) {
                url = details.url;
                username = details.username;
                password = details.password;
                driverClassName = details.driverClassName;
            }
            if (url == null && mysqlHost != null && !mysqlHost.trim().isEmpty()) {
                String mysqlPort = System.getenv("MYSQLPORT") != null ? System.getenv("MYSQLPORT") : "3306";
                String mysqlDb = System.getenv("MYSQLDATABASE") != null ? System.getenv("MYSQLDATABASE") : "railway";
                url = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC";
                username = System.getenv("MYSQLUSER") != null ? System.getenv("MYSQLUSER") : "root";
                password = System.getenv("MYSQLPASSWORD");
                driverClassName = "com.mysql.cj.jdbc.Driver";
            }
        } else {
            // Default to local H2 database to use the preloaded/packaged database by default
            System.out.println("DataSourceConfig: No DB_PROVIDER specified. Defaulting to packaged H2 database to preserve preloaded state.");
        }

        if (url == null) {
            System.out.println("DataSourceConfig: No remote database connection established. Using local H2 fallback...");
            url = "jdbc:h2:file:./ecommerce_db;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL";
            username = "sa";
            password = "";
            driverClassName = "org.h2.Driver";
        }

        System.out.println("DataSourceConfig resolved JDBC URL: " + url);
        System.out.println("DataSourceConfig resolved Username: " + username);

        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    private DatabaseConnectionDetails parseDatabaseUrl(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            return null;
        }
        try {
            String temp = databaseUrl;
            
            // Extract scheme
            String scheme = "postgresql";
            if (temp.contains("://")) {
                String[] schemeParts = temp.split("://", 2);
                scheme = schemeParts[0];
                temp = schemeParts[1];
            }
            
            // Extract query parameters
            String query = "";
            if (temp.contains("?")) {
                String[] queryParts = temp.split("\\?", 2);
                temp = queryParts[0];
                query = queryParts[1];
            }
            
            // Extract userInfo and hostPart using last '@' (handles passwords with '@')
            String userInfo = null;
            String hostPart = temp;
            int lastAtIndex = temp.lastIndexOf('@');
            if (lastAtIndex != -1) {
                userInfo = temp.substring(0, lastAtIndex);
                hostPart = temp.substring(lastAtIndex + 1);
            }
            
            // Extract database name (first '/' separates host/port from database path)
            String dbName = "";
            int firstSlashIndex = hostPart.indexOf('/');
            if (firstSlashIndex != -1) {
                dbName = hostPart.substring(firstSlashIndex + 1);
                hostPart = hostPart.substring(0, firstSlashIndex);
            }
            
            String username = null;
            String password = null;
            // Parse userInfo (first ':' separates username from password)
            if (userInfo != null) {
                int firstColonIndex = userInfo.indexOf(':');
                if (firstColonIndex != -1) {
                    username = userInfo.substring(0, firstColonIndex);
                    password = userInfo.substring(firstColonIndex + 1);
                } else {
                    username = userInfo;
                }
            }

            // Map scheme to standard JDBC details
            String jdbcScheme = "postgresql";
            String driverClassName = "org.postgresql.Driver";
            if (scheme.startsWith("postgres")) {
                jdbcScheme = "postgresql";
                driverClassName = "org.postgresql.Driver";
            } else if (scheme.startsWith("mysql")) {
                jdbcScheme = "mysql";
                driverClassName = "com.mysql.cj.jdbc.Driver";
            }

            // Build JDBC URL
            String url = "jdbc:" + jdbcScheme + "://" + hostPart + "/" + dbName;
            if (!query.isEmpty()) {
                url += "?" + query;
            } else if ("mysql".equals(jdbcScheme)) {
                url += "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC";
            }
            
            return new DatabaseConnectionDetails(url, username, password, driverClassName);
        } catch (Exception e) {
            System.err.println("DataSourceConfig: Error parsing DATABASE_URL: " + e.getMessage());
            return null;
        }
    }

    private static class DatabaseConnectionDetails {
        final String url;
        final String username;
        final String password;
        final String driverClassName;

        DatabaseConnectionDetails(String url, String username, String password, String driverClassName) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
        }
    }
}
