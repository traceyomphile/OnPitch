
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageUser {
    private final Connection connection;

    public ManageUser(Connection conn) {
        this.connection = conn;
    }

    public void createUser(String userName, String userEmail, String userPassword, String userRole) {
        // Code to create a new user in the database
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userName);
            statement.setString(2, userEmail);
            statement.setString(3, encryptPassword(userPassword));
            statement.setString(4, userRole);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    public void deleteUser(String userEmail) {
        // Code to delete a user from the database
        String sql = "DELETE FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userEmail);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    public void updateUser(String userEmail, String newUserName, String newUserPassword, String newUserRole) {
        // Code to update a user's information in the database
        String sql = "UPDATE users SET name = ?, password = ?, role = ? WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newUserName);
            statement.setString(2, newUserPassword);
            statement.setString(3, newUserRole);
            statement.setString(4, userEmail);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    public void changeUserName(String userEmail, String newUserName) {
        // Code to change a user's name in the database
        String sql = "UPDATE users SET name = ? WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newUserName);
            statement.setString(2, userEmail);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Error changing user name: " + e.getMessage());
        }
    }

    public void changeUserPassword(String userEmail, String newUserPassword) {
        // Code to change a user's password in the database
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, encryptPassword(newUserPassword));
            statement.setString(2, userEmail);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Error changing user password: " + e.getMessage());
        }
    }

    public void changeUserEmail(String oldUserEmail, String newUserEmail) {
        // Code to change a user's email in the database
        String sql = "UPDATE users SET email = ? WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newUserEmail);
            statement.setString(2, oldUserEmail);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Error changing user email: " + e.getMessage());
        }
    }

    public void changeUserRole(String userEmail, String newUserRole) {
        // Code to change a user's role in the database
        String sql = "UPDATE users SET role = ? WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newUserRole);
            statement.setString(2, userEmail);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Error changing user role: " + e.getMessage());
        }
    }

    public boolean userExists(String userEmail) {
        // Code to check if a user exists in the database
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userEmail);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        catch (SQLException e) {
            System.out.println("Error checking if user exists: " + e.getMessage());
        }
        return false;
    }

    public boolean validateUser(String userEmail, String userPassword) {
        // Code to validate a user's email and password
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userEmail);
            statement.setString(2, encryptPassword(userPassword));
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) == 1;
            }
        }
        catch (SQLException e) {
            System.out.println("Error validating user: " + e.getMessage());
        }
        return false;
    }

    public boolean isUsersTableEmpty() {
        String sql = "SELECT COUNT(*) FROM users";

        try (PreparedStatement statement = this.connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getInt(1) == 0;

        } catch (SQLException e) {
            System.out.println("Error checking if users table is empty: " + e.getMessage());
            return false;
        }
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }
}