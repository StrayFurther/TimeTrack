package strayfurther.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseConnectionChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Database connection successful: " + connection.getMetaData().getDatabaseProductName());

            // Check if the "ttusers" table exists
            ResultSet resultSet = connection.getMetaData().getTables(null, null, "ttusers", null);
            if (resultSet.next()) {
                System.out.println("Table 'ttusers' exists in the database.");
            } else {
                System.out.println("Table 'ttusers' does not exist in the database.");
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
    }
}