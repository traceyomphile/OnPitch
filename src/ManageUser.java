
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ManageUser {

    public static void createUser(String userName, String userEmail, String userPassword, String userRole) {
        // Code to create a new user in the database
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userName);
                statement.setString(2, userEmail);
                statement.setString(3, userPassword);
                statement.setString(4, userRole);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    public static void deleteUser(String userEmail) {
        // Code to delete a user from the database
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userEmail);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    public static void updateUser(String userEmail, String newUserName, String newUserPassword, String newUserRole) {
        // Code to update a user's information in the database
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "UPDATE users SET name = ?, password = ?, role = ? WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newUserName);
                statement.setString(2, newUserPassword);
                statement.setString(3, newUserRole);
                statement.setString(4, userEmail);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    public static void changeUserName(String userEmail, String newUserName) {
        // Code to change a user's name in the database
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "UPDATE users SET name = ? WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newUserName);
                statement.setString(2, userEmail);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error changing user name: " + e.getMessage());
        }
    }

    public static void changeUserPassword(String userEmail, String newUserPassword) {
        // Code to change a user's password in the database
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "UPDATE users SET password = ? WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newUserPassword);
                statement.setString(2, userEmail);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error changing user password: " + e.getMessage());
        }
    }

    public static void changeUserEmail(String oldUserEmail, String newUserEmail) {
        // Code to change a user's email in the database
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "UPDATE users SET email = ? WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newUserEmail);
                statement.setString(2, oldUserEmail);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error changing user email: " + e.getMessage());
        }
    }

    public static void changeUserRole(String userEmail, String newUserRole) {
        // Code to change a user's role in the database
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "UPDATE users SET role = ? WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newUserRole);
                statement.setString(2, userEmail);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error changing user role: " + e.getMessage());
        }
    }

    public static boolean userExists(String userEmail) {
        // Code to check if a user exists in the database
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userEmail);
                var resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking if user exists: " + e.getMessage());
        }
        return false;
    }

    public static boolean validateUser(String userEmail, String userPassword) {
        // Code to validate a user's email and password
        try (Connection connection = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userEmail);
                statement.setString(2, userPassword);
                var resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 1;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error validating user: " + e.getMessage());
        }
        return false;
    }
}