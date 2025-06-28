package com.aron.dead_gate;

import com.aron.dead_gate.controller.UserController;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainController {
    public static void main(String[] args) throws IOException {
        int port = 9001;
        HttpServer server = HttpServer.create(new InetSocketAddress(port),0);

        server.createContext("/api/users", new UserController());
        server.setExecutor(null);
        server.start();
        System.out.println("UserService running on port: "+port+" /api/users");
    }
}
