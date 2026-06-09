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
        String databaseUrl = System.getenv("DATABASE_URL");
        String pgHost = System.getenv("PGHOST");
        String mysqlHost = System.getenv("MYSQLHOST");
        String dbHost = System.getenv("DB_HOST");

        String url = null;
        String username = null;
        String password = null;
        String driverClassName = null;

        if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
            try {
                System.out.println("DataSourceConfig: DATABASE_URL found. Parsing connection details...");
                String cleanUrl = databaseUrl;
                if (cleanUrl.startsWith("postgres://")) {
                    cleanUrl = cleanUrl.replace("postgres://", "postgresql://");
                }
                URI uri = new URI(cleanUrl);
                
                String scheme = uri.getScheme();
                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath();
                
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                if ("postgresql".equalsIgnoreCase(scheme) || "postgres".equalsIgnoreCase(scheme)) {
                    url = "jdbc:postgresql://" + host + ":" + (port == -1 ? 5432 : port) + path;
                    driverClassName = "org.postgresql.Driver";
                } else if ("mysql".equalsIgnoreCase(scheme)) {
                    url = "jdbc:mysql://" + host + ":" + (port == -1 ? 3306 : port) + path + "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC";
                    driverClassName = "com.mysql.cj.jdbc.Driver";
                }
            } catch (URISyntaxException e) {
                System.err.println("DataSourceConfig error parsing DATABASE_URL: " + e.getMessage());
            }
        }

        // Fallback to individual Railway PG environment variables
        if (url == null && pgHost != null && !pgHost.trim().isEmpty()) {
            System.out.println("DataSourceConfig: PGHOST found. Constructing PostgreSQL connection...");
            String pgPort = System.getenv("PGPORT") != null ? System.getenv("PGPORT") : "5432";
            String pgDb = System.getenv("PGDATABASE") != null ? System.getenv("PGDATABASE") : "railway";
            url = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDb;
            username = System.getenv("PGUSER") != null ? System.getenv("PGUSER") : "postgres";
            password = System.getenv("PGPASSWORD");
            driverClassName = "org.postgresql.Driver";
        }

        // Fallback to individual Railway MySQL environment variables
        if (url == null && mysqlHost != null && !mysqlHost.trim().isEmpty()) {
            System.out.println("DataSourceConfig: MYSQLHOST found. Constructing MySQL connection...");
            String mysqlPort = System.getenv("MYSQLPORT") != null ? System.getenv("MYSQLPORT") : "3306";
            String mysqlDb = System.getenv("MYSQLDATABASE") != null ? System.getenv("MYSQLDATABASE") : "railway";
            url = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC";
            username = System.getenv("MYSQLUSER") != null ? System.getenv("MYSQLUSER") : "root";
            password = System.getenv("MYSQLPASSWORD");
            driverClassName = "com.mysql.cj.jdbc.Driver";
        }

        // Fallback to custom/Docker DB_* variables
        if (url == null && dbHost != null && !dbHost.trim().isEmpty()) {
            System.out.println("DataSourceConfig: DB_HOST found. Constructing database connection...");
            String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "5432";
            String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "ecommerce";
            url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            username = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
            password = System.getenv("DB_PASSWORD");
            driverClassName = "org.postgresql.Driver";
        }

        // Final fallback: Use local H2 file database
        if (url == null) {
            System.out.println("DataSourceConfig: No remote database environment variables found. Using fallback local H2 database...");
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
}
