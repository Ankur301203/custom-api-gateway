package com.aron.dead_gate.controller;

import com.aron.dead_gate.config.RouteConfig;
import com.aron.dead_gate.model.User;
import com.aron.dead_gate.service.UserService;
import com.aron.dead_gate.util.PathUtil;
import com.aron.dead_gate.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.util.RouteMatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class UserController implements HttpHandler {
    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException{
        long startTime = System.currentTimeMillis();

        String address = exchange.getLocalAddress().toString();
        String method = exchange.getRequestMethod();
        String fullPath = exchange.getRequestURI().getPath();
        String basePath = exchange.getHttpContext().getPath();
        String relativePath = fullPath.substring(basePath.length());

        log.info("[{}] Incoming request: {} {}", LocalDateTime.now(), method, fullPath);

        try{
            switch(method.toUpperCase()){
                case "GET" -> handleGet(exchange, relativePath);
                case "POST" -> handlePost(exchange);
                case "PUT" -> handlePut(exchange, relativePath);
                case "DELETE" -> handleDelete(exchange, relativePath);
                default -> ResponseUtil.sendError(exchange, 405, "Method not allowed");
            }
        }
        catch(Exception e){
            log.error("Error handling request: {} {}", method, fullPath, e);
            ResponseUtil.sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        log.info("Completed {} {} in {}ms", method, fullPath, endTime-startTime);

    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
//        System.out.println("GET path parts: " + Arrays.toString(parts));

        if(path.equals("") || path.equals("/")){
            List<User> users = userService.getAllUsers();
            String json = ResponseUtil.toJsonArray(users);
            ResponseUtil.writeJson(exchange, 200, json);
        }
        else{
            Integer id = PathUtil.extractId(parts, 1);
            if(id == null){
                ResponseUtil.sendError(exchange, 400, "Invalid id");
                return;
            }

            User user = userService.getUserById(id);
            if(user != null){
                ResponseUtil.writeJson(exchange, 200, user.toJson());
            }
            else{
                ResponseUtil.sendError(exchange, 404, "User not found");
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        try{
            JSONObject json = new JSONObject(body);
            User user = new User();
            user.setId(json.getInt("id"));
            user.setName(json.getString("name"));

            userService.createUser(user);
            ResponseUtil.writeJson(exchange, 201, user.toJson());
        }
        catch(Exception e){
            ResponseUtil.sendError(exchange, 400, "Invalid JSON: " + e.getMessage());
        }
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        Integer id = PathUtil.extractId(parts, 1);
        if(id == null){
            ResponseUtil.sendError(exchange, 400, "Invalid id");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());
        try{
            JSONObject json = new JSONObject(body);
            User updatedUser = new User();
            updatedUser.setId(id);
            updatedUser.setName(json.getString("name"));

            boolean success = userService.updateUser(id, updatedUser);
            if(success){
                ResponseUtil.writeJson(exchange, 200, updatedUser.toJson());
            }
            else{
                ResponseUtil.sendError(exchange, 404, "User not found");
            }
        }
        catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, "Invalid JSON input: " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        Integer id = PathUtil.extractId(parts, 1);
        if(id == null){
            ResponseUtil.sendError(exchange, 400, "Invalid ID in path");
            return;
        }

        boolean success = userService.deleteUser(id);
        if(success){
            String message = "{\"message\":\"User deleted successfully\"}";
            ResponseUtil.writeJson(exchange, 200, message);
        }
        else{
            ResponseUtil.sendError(exchange, 404, "User not found");
        }
    }
}
