package com.networkmonitor.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple HTTP REST API Server for Network Monitor
 * Exposes endpoints for the React frontend
 */
public class APIServer {
    
    private static final int PORT = 8080;
    private HttpServer server;
    private MonitoringService monitoringService;
    
    public APIServer() {
        this.monitoringService = MonitoringService.getInstance();
    }
    
    /**
     * Start the API server
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Register endpoints
        server.createContext("/api/start", new StartHandler());
        server.createContext("/api/stop", new StopHandler());
        server.createContext("/api/status", new StatusHandler());
        
        server.setExecutor(null); // Use default executor
        server.start();
        
        System.out.println("🚀 API Server started on port " + PORT);
        System.out.println("📡 Endpoints:");
        System.out.println("   POST http://localhost:" + PORT + "/api/start");
        System.out.println("   POST http://localhost:" + PORT + "/api/stop");
        System.out.println("   GET  http://localhost:" + PORT + "/api/status");
    }
    
    /**
     * Stop the API server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            monitoringService.stopMonitoring();
            System.out.println("🛑 API Server stopped");
        }
    }
    
    /**
     * Handler for /api/start endpoint
     */
    class StartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Enable CORS
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // Read request body
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    
                    // Parse JSON manually (simple parsing)
                    String mode = extractJsonValue(body, "mode");
                    String[] ips = extractIpsArray(body);
                    
                    // Start monitoring
                    monitoringService.startMonitoring(mode, ips);
                    
                    // Get initial status
                    MonitoringService.MonitoringStatus status = monitoringService.getStatus();
                    String jsonResponse = toJson(status);
                    
                    // Send response
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(jsonResponse.getBytes());
                    os.close();
                    
                } catch (Exception e) {
                    String error = "{\"error\": \"" + e.getMessage() + "\"}";
                    exchange.sendResponseHeaders(500, error.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(error.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        }
    }
    
    /**
     * Handler for /api/stop endpoint
     */
    class StopHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                monitoringService.stopMonitoring();
                String response = "{\"message\": \"Monitoring stopped\"}";
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
    
    /**
     * Handler for /api/status endpoint
     */
    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if ("GET".equals(exchange.getRequestMethod())) {
                MonitoringService.MonitoringStatus status = monitoringService.getStatus();
                String jsonResponse = toJson(status);
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
    
    /**
     * Enable CORS for cross-origin requests
     */
    private void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
    
    /**
     * Simple JSON conversion (manual for no external dependencies)
     */
    private String toJson(MonitoringService.MonitoringStatus status) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"iteration\":").append(status.iteration).append(",");
        json.append("\"isActive\":").append(status.isActive).append(",");
        
        // Bandwidth
        json.append("\"bandwidth\":{");
        json.append("\"download\":").append(status.bandwidth.download).append(",");
        json.append("\"upload\":").append(status.bandwidth.upload);
        json.append("},");
        
        // Devices
        json.append("\"devices\":[");
        for (int i = 0; i < status.devices.size(); i++) {
            MonitoringService.DeviceDTO device = status.devices.get(i);
            json.append("{");
            json.append("\"ipAddress\":\"").append(device.ipAddress).append("\",");
            json.append("\"macAddress\":\"").append(device.macAddress).append("\",");
            json.append("\"isUp\":").append(device.isUp).append(",");
            json.append("\"latency\":").append(device.latency).append(",");
            json.append("\"packetLoss\":").append(device.packetLoss).append(",");
            json.append("\"uptime\":\"").append(device.uptime).append("\"");
            json.append("}");
            if (i < status.devices.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Extract value from simple JSON string
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return "quick";
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
    
    /**
     * Extract IPs array from JSON
     */
    private String[] extractIpsArray(String json) {
        int start = json.indexOf("\"ips\":[");
        if (start == -1) return new String[0];
        start += 7;
        int end = json.indexOf("]", start);
        String ipsStr = json.substring(start, end);
        
        // Remove quotes and split
        ipsStr = ipsStr.replace("\"", "").trim();
        if (ipsStr.isEmpty()) return new String[0];
        
        return ipsStr.split(",");
    }
    
    /**
     * Main method to run the server
     */
    public static void main(String[] args) {
        try {
            APIServer server = new APIServer();
            server.start();
            
            System.out.println("\n✅ Network Monitor API Server is running");
            System.out.println("💡 Start the React frontend with: npm run dev");
            System.out.println("🛑 Press Ctrl+C to stop the server\n");
            
            // Keep server running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
