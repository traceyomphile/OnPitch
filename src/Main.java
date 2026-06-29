
import java.io.BufferedReader;
import java.io.IOException;


public class Main {

    private static boolean strongPassword(String password) {
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

    private static void signUp(BufferedReader reader) {
        System.out.println("SignUp\n");
        System.out.println("Enter your email:");
        String email;
        String name;
        String password;
        String role;

        try {
            email = reader.readLine();

            if (ManageUser.userExists(email)) {
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

            ManageUser.createUser(name, email, password, role);

            System.out.println("User created successfully! Automatically logging in...\n");
            logIn(reader);
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void logIn(BufferedReader reader) {
        System.out.println("LogIn\n");
        System.out.println("Enter your email:");
        String email = null;
        String password = null;

        try {
            email = reader.readLine();
            System.out.println("Enter your password:");
            password = reader.readLine();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        if (ManageUser.validateUser(email, password)) {
            System.out.println("Successfully logged in!");
        } else {
            System.out.println("Invalid email or password.");
        }
    }

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(System.in))) {
            System.out.println("Welcome to OnPitch!\n");

            System.out.println("LogIn or SignUp:");
            String userInput = reader.readLine();

            if (userInput.equalsIgnoreCase("SignUp")) {
                signUp(reader);
            } else if (userInput.equalsIgnoreCase("LogIn")) {
                logIn(reader);
            } else {
                System.out.println("Invalid input. Please enter 'LogIn' or 'SignUp'.");
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

    }
}
