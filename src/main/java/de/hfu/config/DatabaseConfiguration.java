package de.hfu.config;

import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sets up the embedded H2 database and creates the tables from schema.sql.
 * By default the database lives in memory and is reset on every restart;
 * set app.datasource.url to a file URL to keep the data.
 */
@Configuration
public class DatabaseConfiguration {

    @Bean(destroyMethod = "dispose")
    public JdbcConnectionPool dataSource(@Value("${app.datasource.url}") String url) throws SQLException, IOException {
        JdbcConnectionPool pool = JdbcConnectionPool.create(url, "sa", "");
        createSchema(pool);
        return pool;
    }

    private static void createSchema(DataSource dataSource) throws SQLException, IOException {
        String script = StreamUtils.copyToString(
                new ClassPathResource("schema.sql").getInputStream(), StandardCharsets.UTF_8);
        try (Connection con = dataSource.getConnection(); Statement st = con.createStatement()) {
            for (String sql : script.split(";")) {
                String withoutComments = sql.replaceAll("(?m)^\\s*--.*$", "").trim();
                if (!withoutComments.isEmpty()) {
                    st.execute(withoutComments);
                }
            }
        }
    }
}
