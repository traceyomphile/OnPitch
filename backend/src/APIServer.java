
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class APIServer {
    // Lightweight JSON handling to avoid external dependencies
    private static ManageUser manageUser;

    public static void main(String[] args) {
        try {
            Connection connection = DBConnection.getConnection();
            DBConnection.createUserTable(connection);
            manageUser = new ManageUser(connection);

            HttpServer server = HttpServer.create(new InetSocketAddress(CONFIG.PORT), 0);
            server.createContext("/api/health", APIServer::handleHealth);
            server.createContext("/api/auth/signup", APIServer::handleSignup);
            server.createContext("/api/auth/login", APIServer::handleLogin);
            server.setExecutor(null);
            server.start();

            System.out.println("OnPitch API running at http://localhost:" + CONFIG.PORT);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            System.out.println("Failed to start API server: " + e.getMessage());
        }
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) {
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, response(false, "Method not allowed"));
            return;
        }

        Map<String, Object> body = response(true, "API is running");
        body.put("service", "OnPitch API");
        sendJson(exchange, 200, body);
    }

    private static void handleSignup(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) {
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, response(false, "Method not allowed"));
            return;
        }

        Map<String, String> body = readJson(exchange);
        String name = trim(body.get("name"));
        String email = trim(body.get("email"));
        String password = body.get("password");
        String role = trim(body.get("role"));

        if (name.isEmpty() || email.isEmpty() || password == null || password.isEmpty() || role.isEmpty()) {
            sendJson(exchange, 400, response(false, "Name, email, password, and role are required."));
            return;
        }

        if (!email.matches("\\S+@\\S+\\.\\S+")) {
            sendJson(exchange, 400, response(false, "Enter a valid email address."));
            return;
        }

        if (!strongPassword(password)) {
            sendJson(exchange, 400, response(false, "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."));
            return;
        }

        role = normaliseRole(role);
        if (role == null) {
            sendJson(exchange, 400, response(false, "Role must be Player or Coach."));
            return;
        }

        if (manageUser.userExists(email)) {
            sendJson(exchange, 409, response(false, "User with this email already exists. Please log in."));
            return;
        }

        boolean created = manageUser.createUser(name, email, password, role);
        if (!created) {
            sendJson(exchange, 500, response(false, "Could not create user."));
            return;
        }

        Map<String, Object> response = response(true, "User created successfully.");
        response.put("user", manageUser.getUserByEmail(email));
        sendJson(exchange, 201, response);
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) {
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, response(false, "Method not allowed"));
            return;
        }

        Map<String, String> body = readJson(exchange);
        String email = trim(body.get("email"));
        String password = body.get("password");

        if (email.isEmpty() || password == null || password.isEmpty()) {
            sendJson(exchange, 400, response(false, "Email and password are required."));
            return;
        }

        User user = manageUser.validateUser(email, password);
        if (user == null) {
            sendJson(exchange, 401, response(false, "Invalid email or password."));
            return;
        }

        Map<String, Object> response = response(true, "Successfully logged in.");
        response.put("user", user);
        sendJson(exchange, 200, response);
    }

    private static Map<String, String> readJson(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            if (inputStream == null) {
                return new HashMap<>();
            }
            String body = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
            return parseSimpleJson(body);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Map<String, Object> body) throws IOException {
        addCorsHeaders(exchange);
        String json = toJson(body);
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    // Very simple JSON parser for flat string key->string value objects. Not robust for complex JSON.
    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        // split by commas not inside quotes - simple approach assuming no escaped quotes
        String[] parts = json.split(",(?=(?:[^\"]*\\\"[^\"]*\\\")*[^\"]*$)");
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim();
            String val = kv[1].trim();
            key = stripQuotes(key);
            val = stripQuotes(val);
            map.put(key, val);
        }
        return map;
    }

    private static String stripQuotes(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replaceAll("\\\\\"", "\"");
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escapeJson(e.getKey())).append('"').append(':');
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v.toString());
            } else {
                sb.append('"').append(escapeJson(v.toString())).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, Object> response(boolean success, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        return body;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normaliseRole(String role) {
        if ("player".equalsIgnoreCase(role)) {
            return "Player";
        }
        if ("coach".equalsIgnoreCase(role)) {
            return "Coach";
        }
        return null;
    }

    private static boolean strongPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        return password.matches(".*[!@#$%^&*()].*");
    }
}