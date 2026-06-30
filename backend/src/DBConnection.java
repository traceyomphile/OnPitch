import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        try {
            Files.createDirectories(Path.of("Database"));
        } catch (IOException e) {
            throw new SQLException("Could not create Database folder", e);
        }
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection(CONFIG.URL_STRING, CONFIG.USERNAME, CONFIG.PASSWORD);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static void createUserTable(Connection connection) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL,"
                + "email VARCHAR(100) NOT NULL UNIQUE,"
                + "password VARCHAR(100) NOT NULL,"
                + "role VARCHAR(50) NOT NULL DEFAULT 'user'"
                + ")";

        try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
            statement.execute();
        } catch (SQLException e) {
            System.out.println("Error creating users table: " + e.getMessage());
        }
    }
}
