package com.aron.dead_gate.service;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MockBackendService1 {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9001), 0);
        server.createContext("/api/hello", new HelloHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Mock Backend Service 1 running on http://localhost:9001/api/hello");
    }

    static class HelloHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"message\": \"Hello from Service 1\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
