package com.aron.dead_gate.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseUtil {

    public static void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, body.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes());
        }
    }

    public static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String json = String.format("{\"error\": \"%s\"}", message);
        writeJson(exchange, code, json);
    }
}
