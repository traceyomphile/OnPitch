
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;


public class Main {
    private static Connection connection;
    private static ManageUser manageUser;
    private static BufferedReader reader;

    public Main(Connection conn, BufferedReader br) {
        connection = conn;
        reader = br;

        // Create the users table if it doesn't exist
        DBConnection.createUserTable(connection);
        manageUser = new ManageUser(connection);
    }

    private boolean strongPassword(String password) {
        // Check if the password is at least 8 characters long
        if (password.length() < 8) {
            return false;
        }

        // Check if the password contains at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check if the password contains at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Check if the password contains at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        // Check if the password contains at least one special character
        return password.matches(".*[!@#$%^&*()].*");
    }

    private void signUp() {
        System.out.println("======SIGN UP======");
        System.out.println("\nEnter your email:");
        String email;
        String name;
        String password;
        String role;

        try {
            email = reader.readLine();

            if (!manageUser.isUsersTableEmpty() && manageUser.userExists(email)) {
                System.out.println("User with this email already exists. Please log in.");
                return;
            }

            System.out.println("Enter your username:");
            name = reader.readLine();

            System.out.println("Enter your password:");
            password = reader.readLine();

            if (!strongPassword(password)) {
                System.out.println("Password is not strong enough. Please use a password with at least 8 characters, including uppercase, lowercase, digit, and special character.");
                return;
            }

            System.out.println("Enter your role (Player/Coach):");
            role = reader.readLine();

            if (!role.equalsIgnoreCase("Player") && !role.equalsIgnoreCase("Coach")) {
                System.out.println("Invalid role. Please enter either 'Player' or 'Coach'.");
                return;
            }

            manageUser.createUser(name, email, password, role);

            System.out.println("User created successfully! Automatically logging in...\n");
            logIn();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void logIn() {
        System.out.println("======LOG IN======");
        System.out.println("\nEnter your email:");
        String email = null;
        String password = null;

        try {
            email = reader.readLine();
            System.out.println("Enter your password:");
            password = reader.readLine();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        if (manageUser.validateUser(email, password)) {
            System.out.println("Successfully logged in!");
        } else {
            System.out.println("Invalid email or password.");
        }
    }

    private String readInput() {
        String input = null;
        try {
            input = reader.readLine();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        while (input == null || input.trim().isEmpty()) {
            System.out.println("Input cannot be empty. Please try again.");
            input = readInput(); // Recursively prompt for input again
        }
        return input;
    }
    public static void main(String[] args) {
        // Connect to DB
        try (Connection conn = DBConnection.getConnection();
             BufferedReader br = new BufferedReader(new java.io.InputStreamReader(System.in))) {
            
            // Initialize Main with the connection and reader
            Main mainApp = new Main(conn, br);

            System.out.println("======WELCOME TO ONPITCH======");

            System.out.println("\nEnter 'LogIn' or 'SignUp':");
            String userInput = mainApp.readInput();

            if (userInput.equalsIgnoreCase("SignUp")) {
                mainApp.signUp();
            } else if (userInput.equalsIgnoreCase("LogIn")) {
                mainApp.logIn();
            } else {
                System.out.println("Invalid input. Please enter 'LogIn' or 'SignUp'.");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

    }
}
