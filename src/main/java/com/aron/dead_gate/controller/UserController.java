package com.aron.dead_gate.controller;

import com.aron.dead_gate.model.User;
import com.aron.dead_gate.service.UserService;
import com.aron.dead_gate.util.PathUtil;
import com.aron.dead_gate.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class UserController implements HttpHandler {
    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String fullPath = exchange.getRequestURI().getPath();
        String basePath = exchange.getHttpContext().getPath();
        String relativePath = fullPath.substring(basePath.length());

//        System.out.println("Incoming request: "+fullPath);

        switch(method.toUpperCase()){
            case "GET" -> handleGet(exchange, relativePath);
            case "POST" -> handlePost(exchange, relativePath);
            case "PUT" -> handlePut(exchange, relativePath);
            case "DELETE" -> handleDelete(exchange, relativePath);
            default -> ResponseUtil.sendError(exchange, 405, "Method not allowed");
        }
    }

    private void handleDelete(HttpExchange exchange, String relativePath) throws IOException{
        String[] parts = relativePath.split("/");
        Integer id = PathUtil.extractId(parts,1);
        if(id == null){
            ResponseUtil.sendError(exchange,400,"Invalid ID in path");
            return;
        }
        boolean flag = userService.deleteUser(id);
        if(flag){
            ResponseUtil.writeJson(exchange,204,"{\"message\":\"User deleted\"}");
        }
        else{
            ResponseUtil.sendError(exchange,404,"User not found");
        }
    }

    private void handlePut(HttpExchange exchange, String relativePath) throws IOException{
        String[] parts = relativePath.split("/");
        Integer id = PathUtil.extractId(parts,1);
        if(id == null){
            ResponseUtil.sendError(exchange,400,"Invalid id");
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes());
        try{
            JSONObject json = new JSONObject(body);

            User updatedUser = new User();
            updatedUser.setId(id);
            updatedUser.setName(json.getString("name"));

            boolean flag = userService.updateUser(id, updatedUser);
            if(flag){
                ResponseUtil.writeJson(exchange,200,updatedUser.toJson());
            }
            else{
                ResponseUtil.sendError(exchange,404,"User not found");
            }
        }
        catch (Exception e){
            ResponseUtil.sendError(exchange, 400, "Invalid JSON input: "+e.getMessage());
        }
    }

    private void handlePost(HttpExchange exchange, String relativePath) throws IOException{
        String body = new String(exchange.getRequestBody().readAllBytes());
        try{
            JSONObject json = new JSONObject(body);

            User user = new User();
            user.setId(json.getInt("id"));
            user.setName(json.getString("name"));

            userService.createUser(user);
            ResponseUtil.writeJson(exchange,201,user.toJson());

        }
        catch (Exception e){
            ResponseUtil.sendError(exchange, 400, "Invalid request body");
        }

    }

    private void handleGet(HttpExchange exchange, String path) throws IOException{
        String[] parts = path.split("/");
        System.out.println("Incoming request: "+path);
        System.out.println("Incoming request parts: "+Arrays.toString(parts));
        if(parts.length == 0 || parts[0].isEmpty()){
            List<User> users = userService.getAllUsers();
            String json = ResponseUtil.toJsonArray(users);
            ResponseUtil.writeJson(exchange,200,json);
        }
        else{
            // /api/users/{id}
            Integer id = PathUtil.extractId(parts,1);
            if(id == null){
                ResponseUtil.sendError(exchange,400,"Invalid id");
                return;
            }

            User user = userService.getUserById(id);
            if(user != null){
                ResponseUtil.writeJson(exchange,200,user.toJson());
            }
            else{
                ResponseUtil.sendError(exchange,404,"User not found");
            }
        }
    }
}
