package com.aron.dead_gate.apiTest;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;

public class PostLoadTester {

    // Number of total requests
    private static final int TOTAL_REQUESTS = 1000;
    // Number of threads (simulates concurrency)
    private static final int THREADS = 50;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            final int id = i + 1000; // Ensure unique IDs
            executor.submit(() -> {
                try {
                    sendPost(id, "Ankur");
                } catch (Exception e) {
                    System.err.println("Failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Completed " + TOTAL_REQUESTS + " requests in " + duration + "ms");
    }

    private static void sendPost(int id, String name) throws Exception {
        String urlString = "http://localhost:8080/service1/api/users";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = String.format("{\"id\":%d,\"name\":\"%s\"}", id, name);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        int code = conn.getResponseCode();
        if (code != 201 && code != 200) {
            System.err.println("Non-OK response: " + code + " for ID: " + id);
        }
    }
}

