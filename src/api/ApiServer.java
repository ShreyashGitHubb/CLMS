package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.EquipmentDao;
import dao.TransactionDao;
import exception.EquipmentNotAvailableException;
import exception.InvalidTransactionException;
import model.Equipment;
import service.BorrowService;
import service.EquipmentService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiServer {
    private static final int PORT = 8080;
    private static final Pattern NUMBER_FIELD = Pattern.compile("\\\"(userId|equipmentId)\\\"\\s*:\\s*(\\d+)");
    private static final Pattern DATE_FIELD = Pattern.compile("\\\"dueDate\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private final EquipmentService equipmentService = new EquipmentService(new EquipmentDao());
    private final BorrowService borrowService = new BorrowService(new EquipmentDao(), new TransactionDao());

    public static void main(String[] args) throws IOException {
        new ApiServer().start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/equipment", this::handleEquipment);
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

    private void handleRequest(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (handleOptions(exchange) || !"POST".equals(exchange.getRequestMethod())) {
            return;
        }
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int userId = numberField(body, "userId");
            int equipmentId = numberField(body, "equipmentId");
            LocalDate dueDate = LocalDate.parse(dateField(body));
            int transactionId = borrowService.requestEquipment(userId, equipmentId, dueDate);
            writeJson(exchange, 201, "{\"transactionId\":" + transactionId + ",\"status\":\"PENDING\"}");
        } catch (InvalidTransactionException | EquipmentNotAvailableException exception) {
            writeError(exchange, 400, exception.getMessage());
        } catch (Exception exception) {
            writeError(exchange, 500, exception.getMessage());
        }
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

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
