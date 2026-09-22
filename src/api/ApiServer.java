package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.EquipmentDao;
import dao.TransactionDao;
import exception.EquipmentNotAvailableException;
import exception.InvalidTransactionException;
import model.Equipment;
import model.BorrowTransaction;
import model.User;
import service.BorrowService;
import service.AuthenticationService;
import service.EquipmentService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApiServer {
    private static final int PORT = 8080;
    private static final Pattern NUMBER_FIELD = Pattern.compile("\\\"(userId|equipmentId)\\\"\\s*:\\s*(\\d+)");
    private static final Pattern DATE_FIELD = Pattern.compile("\\\"dueDate\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern EMAIL_FIELD = Pattern.compile("\\\"email\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern PASSWORD_FIELD = Pattern.compile("\\\"password\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private final EquipmentService equipmentService = new EquipmentService(new EquipmentDao());
    private final BorrowService borrowService = new BorrowService(new EquipmentDao(), new TransactionDao());
    private final AuthenticationService authenticationService = new AuthenticationService(new dao.UserDao());
    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        new ApiServer().start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/equipment", this::handleEquipment);
        server.createContext("/api/auth/login", this::handleLogin);
        server.createContext("/api/requests", this::handleRequest);
        server.setExecutor(null);
        server.start();
        System.out.println("CLMS API running at http://localhost:" + PORT);
    }

    private void handleEquipment(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (handleOptions(exchange) || !"GET".equals(exchange.getRequestMethod())) {
            return;
        }
        try {
            writeJson(exchange, 200, equipmentJson(equipmentService.listAvailable()));
        } catch (Exception exception) {
            writeError(exchange, 500, exception.getMessage());
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (handleOptions(exchange) || !"POST".equals(exchange.getRequestMethod())) {
            return;
        }
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            User user = authenticationService.authenticate(stringField(body, EMAIL_FIELD, "email"), stringField(body, PASSWORD_FIELD, "password"));
            String token = UUID.randomUUID().toString();
            sessions.put(token, user);
                writeJson(exchange, 200, "{\"token\":\"" + token + "\",\"user\":{"
                    + "\"id\":" + user.id() + ",\"name\":\"" + escape(user.fullName())
                    + "\",\"email\":\"" + escape(user.email()) + "\",\"role\":\"" + user.role() + "\"}}");
        } catch (IllegalArgumentException exception) {
            writeError(exchange, 401, exception.getMessage());
        } catch (Exception exception) {
            writeError(exchange, 500, exception.getMessage());
        }
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (handleOptions(exchange)) {
            return;
        }
        try {
            User authenticatedUser = authenticatedUser(exchange);
            String path = exchange.getRequestURI().getPath();
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                int userId = authenticatedUser.role() == User.Role.STUDENT ? authenticatedUser.id()
                        : query != null && query.startsWith("userId=") ? Integer.parseInt(query.substring("userId=".length())) : 0;
                writeJson(exchange, 200, transactionsJson(userId));
                return;
            }
            if (path.matches("/api/requests/\\d+/(approve|return)")) {
                int transactionId = Integer.parseInt(path.split("/")[3]);
                if (path.endsWith("/approve")) {
                    if (authenticatedUser.role() == User.Role.STUDENT) {
                        writeError(exchange, 403, "Only lab staff can approve requests.");
                        return;
                    }
                    borrowService.approveRequest(transactionId, authenticatedUser.id());
                } else {
                    borrowService.returnEquipment(transactionId);
                }
                writeJson(exchange, 200, "{\"status\":\"updated\"}");
                return;
            }
            if (!"POST".equals(exchange.getRequestMethod())) {
                writeError(exchange, 405, "Method not allowed");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int userId = authenticatedUser.id();
            int equipmentId = numberField(body, "equipmentId");
            LocalDate dueDate = LocalDate.parse(dateField(body));
            int transactionId = borrowService.requestEquipment(userId, equipmentId, dueDate);
            writeJson(exchange, 201, "{\"transactionId\":" + transactionId + ",\"status\":\"PENDING\"}");
        } catch (InvalidTransactionException | EquipmentNotAvailableException exception) {
            writeError(exchange, 400, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            writeError(exchange, 401, exception.getMessage());
        } catch (Exception exception) {
            writeError(exchange, 500, exception.getMessage());
        }
    }

    private User authenticatedUser(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authentication required.");
        }
        User user = sessions.get(header.substring("Bearer ".length()));
        if (user == null) {
            throw new IllegalArgumentException("Invalid session.");
        }
        return user;
    }

    private boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    }

    private void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private void writeError(HttpExchange exchange, int status, String message) throws IOException {
        writeJson(exchange, status, "{\"error\":\"" + escape(message == null ? "Unknown error" : message) + "\"}");
    }

    private String equipmentJson(List<Equipment> equipment) {
        return "[" + equipment.stream().map(item -> "{"
                + "\"id\":" + item.id() + ",\"name\":\"" + escape(item.name()) + "\","
                + "\"category\":\"" + escape(item.category()) + "\",\"description\":\"" + escape(item.description()) + "\","
                + "\"total\":" + item.totalQuantity() + ",\"available\":" + item.availableQuantity() + ","
                + "\"location\":\"" + escape(item.location()) + "\"}").collect(java.util.stream.Collectors.joining(",")) + "]";
    }

    private String transactionsJson(int userId) throws java.sql.SQLException {
        try (var connection = config.DatabaseConnection.open()) {
            List<BorrowTransaction> transactions = userId > 0
                    ? new TransactionDao().findByUser(connection, userId)
                    : new TransactionDao().findAll(connection);
            return "[" + transactions.stream().map(transaction -> "{"
                    + "\"id\":" + transaction.id() + ",\"userId\":" + transaction.userId()
                    + ",\"equipmentId\":" + transaction.equipmentId() + ",\"issueDate\":" + nullable(transaction.issueDate())
                    + ",\"dueDate\":\"" + transaction.dueDate() + "\",\"returnDate\":" + nullable(transaction.returnDate())
                    + ",\"status\":\"" + transaction.status() + "\",\"fine\":" + transaction.fineAmount() + "}")
                    .collect(java.util.stream.Collectors.joining(",")) + "]";
        }
    }

    private String nullable(LocalDate date) {
        return date == null ? "null" : "\"" + date + "\"";
    }

    private int numberField(String body, String field) {
        Matcher matcher = NUMBER_FIELD.matcher(body);
        while (matcher.find()) {
            if (field.equals(matcher.group(1))) {
                return Integer.parseInt(matcher.group(2));
            }
        }
        throw new IllegalArgumentException("Missing field: " + field);
    }

    private String dateField(String body) {
        Matcher matcher = DATE_FIELD.matcher(body);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing field: dueDate");
        }
        return matcher.group(1);
    }

    private String stringField(String body, Pattern pattern, String field) {
        Matcher matcher = pattern.matcher(body);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing field: " + field);
        }
        return matcher.group(1);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
