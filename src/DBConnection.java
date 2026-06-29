import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection()  throws SQLException {
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

    public static void createUserTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "userName VARCHAR(100) NOT NULL,"
                + "userEmail VARCHAR(100) NOT NULL UNIQUE,"
                + "userPassword VARCHAR(100) NOT NULL,"
                + "userRole VARCHAR(50) NOT NULL DEFAULT 'user'"
                + ")";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
            statement.execute();
        } catch (SQLException e) {
            System.out.println("Error creating users table: " + e.getMessage());
        }
    }
}