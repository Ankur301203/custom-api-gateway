package com.aron.dead_gate.controller;

import com.aron.dead_gate.model.User;
import com.aron.dead_gate.service.UserService;
import com.aron.dead_gate.util.PathUtil;
import com.aron.dead_gate.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;

public class UserController implements HttpHandler {
    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if(!"GET".equalsIgnoreCase(exchange.getRequestMethod())){
            ResponseUtil.sendError(exchange,405,"Method not allowed");
            return;
        }

        if(parts.length == 3){
            List<User> users = userService.getAllUsers();
            String json = users.stream()
                    .map(User::toJson)
                    .reduce("[",(a,b)->a.equals("[") ? a+b : a+","+b) + "]";
            json += "]";
            ResponseUtil.writeJson(exchange,200,json);

        }
        else if(parts.length == 4){
            Integer id = PathUtil.extractId(parts,4);
            if(id == null){
                ResponseUtil.sendError(exchange,400,"Invalid id");
                return;
            }
            User user = userService.getUserById(id);
            if(user != null){
                ResponseUtil.writeJson(exchange,200,user.toJson());
            }
            else{
                ResponseUtil.writeJson(exchange,404,"User not found");
            }
        }
        else{
            ResponseUtil.sendError(exchange,404,"Invalid path");
        }
    }
}
