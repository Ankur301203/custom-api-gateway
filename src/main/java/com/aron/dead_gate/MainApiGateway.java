package com.aron.dead_gate;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;

public class MainApiGateway {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port),0);
        System.out.println("Api gateway started on port: "+port);
        
        Map<String,String> routeMap = Map.of(
                "service1","http://localhost:9001",
                "service2","http://localhost:9002"

        );

        server.createContext("/", new DynamicRouter(routeMap));
        server.setExecutor(Executors.newFixedThreadPool(10)); // thread pool
        server.start();

    }
    static class DynamicRouter implements HttpHandler {
        private final Map<String,String> routeMap;

        public DynamicRouter(Map<String,String> routeMap){
            this.routeMap = routeMap;
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            String path = exchange.getRequestURI().getPath();
            System.out.println("Incoming request: "+path);

            String[] parts = path.split("/");

            if(parts.length < 2){
                return;
            }

            String serviceName = parts[1];
            String backendUrl = routeMap.get(serviceName);
            if(backendUrl == null){
                sendError(exchange,404,"Service not found: "+serviceName);
                return;
            }
            String forwardedPath = path.replaceFirst("/"+serviceName,"");

            String servicePath = path.replaceFirst("/service1", ""); // remove service prefix only
            String fullUrl = backendUrl + servicePath;

            System.out.println("Forwarding to backend: "+fullUrl);

            try{
                HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
                conn.setRequestMethod(exchange.getRequestMethod());
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // copying headers from exchange
                exchange.getRequestHeaders().forEach((key, values) -> {
                    for (String value : values) {
                        conn.addRequestProperty(key, value);
                    }
                });

                // forwarding req body
                if(exchange.getRequestMethod().equalsIgnoreCase("POST") ||
                    exchange.getRequestMethod().equalsIgnoreCase("PUT")){
                    conn.setDoOutput(true);
                    try(OutputStream out = conn.getOutputStream(); InputStream in = exchange.getRequestBody()){
                        in.transferTo(out);
                    }
                }

                int responseCode = conn.getResponseCode();
                InputStream backendResponseStream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
                byte[] responseBytes = backendResponseStream.readAllBytes();

                exchange.sendResponseHeaders(responseCode,responseBytes.length);
                try(OutputStream os = exchange.getResponseBody()){
                    os.write(responseBytes);
                }

                System.out.println("Backend responded with code: "+responseCode);
            }
            catch (Exception e){
                e.printStackTrace();
                sendError(exchange,502,"Gateway error: "+e.getMessage());
            }
        }
        private void sendError(HttpExchange exchange, int code, String message) throws IOException{
            byte[] bytes = message.getBytes();
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class ProxyHandler implements HttpHandler {
        private final String backendUrl;

        public ProxyHandler(String backendUrl){
            this.backendUrl = backendUrl;
        }

        @Override
        public void handle(HttpExchange exchange)throws IOException{
            String fullUrl = backendUrl + exchange.getRequestURI().getPath().replaceFirst("/[^/]+","");
            System.out.println("Incoming request: " + exchange.getRequestURI());
            System.out.println("Forwarding to backend: " + fullUrl);

            try {
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
//                conn.setRequestMethod(exchange.getRequestMethod());

                exchange.getRequestHeaders().forEach((key,values)->{
                    for(String value : values){
                        conn.addRequestProperty(key, value);
                    }
                });

                // if post/put req
                if(exchange.getRequestMethod().equalsIgnoreCase("POST") || exchange.getRequestMethod().equalsIgnoreCase("PUT")){
                    conn.setDoOutput(true);
                    try(OutputStream out = conn.getOutputStream(); InputStream in = exchange.getRequestBody()){
                        in.transferTo(out);
                    }
                }
                int responseCode = conn.getResponseCode();
                InputStream backendResponseStream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
                byte[] responseBytes = backendResponseStream.readAllBytes();

                exchange.sendResponseHeaders(responseCode,responseBytes.length);
                try(OutputStream os = exchange.getResponseBody()){
                    os.write(responseBytes);
                }

                System.out.println("Backend responded with code: "+responseCode);
            }
            catch (IOException e) {
                e.printStackTrace();
                String err = "Gateway error: "+e.getMessage();
                exchange.sendResponseHeaders(502,err.length());
                try(OutputStream os = exchange.getResponseBody()){
                    os.write(err.getBytes());
                }
            }

        }
    }
}