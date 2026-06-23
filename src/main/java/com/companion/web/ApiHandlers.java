package com.companion.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.companion.auth.UserDAO;
import com.companion.auth.UserRecord;
import com.companion.auth.Session;
import com.companion.auth.InteractionDAO;
import com.companion.admin.AnalyticsDAO;
import com.companion.gokhul.UserPreferences;
import com.companion.navis.RecommendationEngine;
import com.companion.navis.ScoredResult;
import com.companion.gokhul.Item;
import com.companion.navis.CsvItem;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;

public class ApiHandlers {

    private static UserDAO userDAO = new UserDAO();
    private static RecommendationEngine recommendationEngine = new RecommendationEngine();
    private static InteractionDAO interactionDAO = new InteractionDAO();
    private static AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    public static void registerHandlers(HttpServer server) {
        server.createContext("/api/login", handleLogin());
        server.createContext("/api/register", handleRegister());
        server.createContext("/api/recommendations", handleRecommendations());
        server.createContext("/api/interact", handleInteract());
        server.createContext("/api/stats", handleStats());
        server.createContext("/api/recent_searches", handleRecentSearches());
        server.createContext("/api/logout", handleLogout());
        server.createContext("/api/import", handleImport());
    }

    private static HttpHandler handleLogin() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange.getRequestBody());
                String username = params.get("username");
                String password = params.get("password");

                UserRecord user = userDAO.login(username, password);
                if (user != null) {
                    Session.getInstance().login(user);
                    sendJsonResponse(exchange, 200, "{\"status\":\"success\", \"role\":\"" + user.getRole() + "\"}");
                } else {
                    sendJsonResponse(exchange, 401, "{\"status\":\"error\", \"message\":\"Invalid credentials\"}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        };
    }
    
    private static HttpHandler handleLogout() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Session.getInstance().logout();
                sendJsonResponse(exchange, 200, "{\"status\":\"success\"}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        };
    }

    private static HttpHandler handleRegister() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange.getRequestBody());
                UserDAO.RegisterResult result = userDAO.register(
                    params.get("username"),
                    params.get("email"),
                    params.get("password"),
                    params.getOrDefault("budget", "Any"),
                    params.getOrDefault("diet", "Any"),
                    "USER"
                );
                
                if (result == UserDAO.RegisterResult.SUCCESS) {
                    sendJsonResponse(exchange, 200, "{\"status\":\"success\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"" + result.name() + "\"}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        };
    }

    private static HttpHandler handleRecommendations() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
                UserPreferences prefs = new UserPreferences();
                prefs.setSearchQuery(query.getOrDefault("q", ""));
                prefs.setDomain(query.getOrDefault("domain", "All"));
                prefs.setDiet(query.getOrDefault("diet", "Any"));
                
                String minPriceStr = query.get("minPrice");
                String maxPriceStr = query.get("maxPrice");
                if (minPriceStr != null && !minPriceStr.isEmpty()) prefs.setMinPrice(Double.parseDouble(minPriceStr));
                if (maxPriceStr != null && !maxPriceStr.isEmpty()) prefs.setMaxPrice(Double.parseDouble(maxPriceStr));
                
                UserRecord current = Session.getInstance().getCurrentUser();
                if (current != null) {
                    if (prefs.getDiet().equals("Any") || prefs.getDiet().isEmpty()) {
                        prefs.setDiet(current.getDietPref());
                    }
                    prefs.setBudget(current.getBudgetPref());
                }

                List<ScoredResult> results = recommendationEngine.getRecommendations(prefs);
                
                if (current != null && !prefs.getSearchQuery().isEmpty()) {
                    analyticsDAO.logSearch(current.getId(), prefs.getSearchQuery(), prefs.getDomain(), results.size());
                }

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < results.size(); i++) {
                    Item item = results.get(i).getItem();
                    json.append("{")
                        .append("\"id\":").append(item.getId()).append(",")
                        .append("\"name\":\"").append(escapeJson(item.getName())).append("\",")
                        .append("\"price\":").append(item.getPrice()).append(",")
                        .append("\"rating\":").append(item.getRating()).append(",")
                        .append("\"domain_category\":\"").append(escapeJson(item.getDomainCategory())).append("\",")
                        .append("\"description\":\"").append(escapeJson(item.getDescription())).append("\",")
                        .append("\"image_path\":\"").append(escapeJson(item.getImagePath())).append("\",")
                        .append("\"external_link\":\"").append(escapeJson(item.getExternalLink())).append("\",")
                        .append("\"score\":").append(results.get(i).getScore())
                        .append("}");
                    if (i < results.size() - 1) json.append(",");
                }
                json.append("]");
                
                sendJsonResponse(exchange, 200, json.toString());
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        };
    }

    private static HttpHandler handleInteract() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                UserRecord current = Session.getInstance().getCurrentUser();
                if (current == null) {
                    sendJsonResponse(exchange, 401, "{\"status\":\"error\", \"message\":\"Unauthorized\"}");
                    return;
                }
                
                Map<String, String> params = parseFormData(exchange.getRequestBody());
                int itemId = Integer.parseInt(params.get("itemId"));
                String action = params.get("action");
                
                if ("like".equals(action)) {
                    interactionDAO.logInteraction(current.getId(), itemId, "like");
                } else if ("unlike".equals(action)) {
                    interactionDAO.removeInteraction(current.getId(), itemId, "like");
                }
                
                sendJsonResponse(exchange, 200, "{\"status\":\"success\"}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        };
    }

    private static HttpHandler handleStats() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                int users = analyticsDAO.getTotalUsers();
                int items = analyticsDAO.getTotalItems();
                int searches = analyticsDAO.getTotalSearches();
                
                String json = String.format("{\"users\":%d, \"items\":%d, \"searches\":%d}", users, items, searches);
                sendJsonResponse(exchange, 200, json);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        };
    }
    
    private static HttpHandler handleRecentSearches() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                UserRecord current = Session.getInstance().getCurrentUser();
                if (current == null) {
                    sendJsonResponse(exchange, 200, "[]");
                    return;
                }
                List<String> searches = analyticsDAO.getRecentSearches(current.getId());
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < searches.size(); i++) {
                    json.append("\"").append(escapeJson(searches.get(i))).append("\"");
                    if (i < searches.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        };
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseFormData(InputStream is) throws IOException {
        String formData = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return parseQuery(formData);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }
    
    private static HttpHandler handleImport() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                UserRecord current = Session.getInstance().getCurrentUser();
                if (current == null || !"admin".equalsIgnoreCase(current.getRole())) {
                    sendJsonResponse(exchange, 403, "{\"status\":\"error\", \"message\":\"Forbidden: Admin only\"}");
                    return;
                }
                
                Map<String, String> params = parseFormData(exchange.getRequestBody());
                String csvPath = params.get("csvPath");
                
                if (csvPath == null || csvPath.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Missing csvPath\"}");
                    return;
                }
                
                File fileToImport = new File(csvPath);
                if (!fileToImport.exists()) {
                    sendJsonResponse(exchange, 404, "{\"status\":\"error\", \"message\":\"File not found\"}");
                    return;
                }
                
                List<CsvItem> itemsToImport = new ArrayList<>();
                int skippedRows = 0;
                
                try (BufferedReader br = new BufferedReader(new FileReader(fileToImport))) {
                    String headerLine = br.readLine();
                    if (headerLine == null) {
                        sendJsonResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Empty CSV file\"}");
                        return;
                    }

                    List<String> headers = parseCsvLine(headerLine);
                    Map<String, Integer> colMap = new HashMap<>();
                    for (int i = 0; i < headers.size(); i++) {
                        colMap.put(headers.get(i).toLowerCase().trim(), i);
                    }

                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        List<String> row = parseCsvLine(line);
                        
                        try {
                            CsvItem item = new CsvItem();
                            item.setName(getValueByHeader(row, colMap, "name", ""));
                            item.setPrice(Double.parseDouble(getValueByHeader(row, colMap, "price", "0.0")));
                            item.setDomainCategory(getValueByHeader(row, colMap, "domain_category", "Shopping & products"));
                            item.setRating(Double.parseDouble(getValueByHeader(row, colMap, "rating", "4.0")));
                            item.setTags(getValueByHeader(row, colMap, "tags", ""));
                            item.setDescription(getValueByHeader(row, colMap, "description", ""));
                            item.setSubCategory(getValueByHeader(row, colMap, "sub_category", ""));
                            item.setDietType(getValueByHeader(row, colMap, "diet_type", null));
                            
                            if (item.isValid()) {
                                itemsToImport.add(item);
                            } else {
                                skippedRows++;
                            }
                        } catch (Exception ex) {
                            skippedRows++;
                        }
                    }
                    
                    boolean success = false;
                    if (!itemsToImport.isEmpty()) {
                        success = analyticsDAO.addItemsBatch(itemsToImport);
                    }
                    
                    if (success || itemsToImport.isEmpty()) {
                        sendJsonResponse(exchange, 200, "{\"status\":\"success\", \"imported\":" + itemsToImport.size() + ", \"skipped\":" + skippedRows + "}");
                    } else {
                        sendJsonResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Database insertion failed\"}");
                    }
                    
                } catch (Exception ex) {
                    sendJsonResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        };
    }

    private static String getValueByHeader(List<String> row, Map<String, Integer> colMap, String columnName, String defaultValue) {
        Integer index = colMap.get(columnName);
        if (index != null && index < row.size()) {
            String val = row.get(index);
            return (val == null || val.isEmpty()) ? defaultValue : val;
        }
        return defaultValue;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString().trim());
        return values;
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
