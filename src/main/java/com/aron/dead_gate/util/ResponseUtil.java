package com.aron.dead_gate.util;

import com.aron.dead_gate.model.User;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ResponseUtil {

    public static void writeJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    public static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        writeJson(exchange, code, "{\"error\":\""+message+"\"}");
    }

    public static String toJsonArray(List<User> users){
        StringBuilder sb = new StringBuilder("[");
        for(int i=0;i<users.size();i++){
            sb.append(users.get(i).toJson());
            if(i < users.size()-1){
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
