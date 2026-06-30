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

    public boolean createUser(String userName, String userEmail, String userPassword, String userRole) {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userName);
            statement.setString(2, userEmail);
            statement.setString(3, encryptPassword(userPassword));
            statement.setString(4, userRole);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public void deleteUser(String userEmail) {
        String sql = "DELETE FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userEmail);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    public void updateUser(String userEmail, String newUserName, String newUserPassword, String newUserRole) {
        String sql = "UPDATE users SET name = ?, password = ?, role = ? WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newUserName);
            statement.setString(2, encryptPassword(newUserPassword));
            statement.setString(3, newUserRole);
            statement.setString(4, userEmail);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    public boolean userExists(String userEmail) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userEmail);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking if user exists: " + e.getMessage());
        }
        return false;
    }

    public User validateUser(String userEmail, String userPassword) {
        String sql = "SELECT name, email, role FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userEmail);
            statement.setString(2, encryptPassword(userPassword));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("role")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error validating user: " + e.getMessage());
        }
        return null;
    }

    public User getUserByEmail(String userEmail) {
        String sql = "SELECT name, email, role FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userEmail);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("role")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user: " + e.getMessage());
        }
        return null;
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
