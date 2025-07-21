package com.aron.dead_gate;

import com.aron.dead_gate.config.RouteConfig;
import com.aron.dead_gate.rate_limit.RateLimiterV1;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;

@Slf4j
public class MainApiGateway {
    private static final RateLimiterV1 rateLim = new RateLimiterV1(1000); // 10 req/sec per service

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port),0);
        System.out.println("Api gateway started on port: "+port);
        
        RouteConfig.loadFromProperties();

        server.createContext("/", new DynamicRouter());
        server.setExecutor(Executors.newFixedThreadPool(100)); // thread pool
        server.start();

    }
    static class DynamicRouter implements HttpHandler {
        public DynamicRouter(){}

        @Override
        public void handle(HttpExchange exchange) throws IOException{
            String path = exchange.getRequestURI().getPath();
            System.out.println("Incoming request: "+path);

            String[] parts = path.split("/");

            if(parts.length < 2){
                return;
            }

            String serviceName = parts[1];
            if(!rateLim.isAllowed(serviceName)){
                sendError(exchange,429,"Rate limit exceeded for service: "+serviceName);
                return;
            }

            String backendUrl = RouteConfig.getNextUrl(serviceName);
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

//                System.out.println("Backend responded with code: "+responseCode);
                log.info("Backend responded with code: {} for request: {} {}", responseCode, exchange.getRequestMethod(), exchange.getRequestURI());
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
}