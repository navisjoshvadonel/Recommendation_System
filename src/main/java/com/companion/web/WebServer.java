package com.companion.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class WebServer {

    private static final int PORT = 8080;
    private static final String WEB_DIR = "web";

    public static void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Serve static files (HTML, JS, CSS)
            server.createContext("/", new StaticFileHandler());
            
            // Register API Endpoints
            ApiHandlers.registerHandlers(server);
            
            server.setExecutor(Executors.newFixedThreadPool(10)); // multi-threaded
            server.start();
            
            System.out.println("==================================================");
            System.out.println("   Web Application Started!");
            System.out.println("   Access the app at: http://localhost:" + PORT);
            System.out.println("==================================================");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            File file = new File(WEB_DIR + path);
            if (!file.exists() || file.isDirectory()) {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                if (path.endsWith(".js")) mimeType = "application/javascript";
                else if (path.endsWith(".css")) mimeType = "text/css";
                else if (path.endsWith(".html")) mimeType = "text/html";
                else mimeType = "application/octet-stream";
            }

            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, file.length());

            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fs = new FileInputStream(file)) {
                final byte[] buffer = new byte[0x10000];
                int count;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
            }
        }
    }
}
