package com.aron.dead_gate.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

@Slf4j
@Component
@ConfigurationProperties(prefix = "routes")
public class RouteConfig {
    private static final Map<String, List<String>> service = new HashMap<>();
    private static final Map<String, AtomicInteger> pointer = new HashMap<>();


    public static void loadFromProperties(){
        try(InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("config.properties")){
            if(is == null){
                throw new RuntimeException("config.properties not found");
            }
            Properties props = new Properties();
            props.load(is);
            for(String name : props.stringPropertyNames()){
                if(name.startsWith("routes.")){
                    String rawKey = name.substring("routes.".length());  // e.g. service1[0]
                    String serviceName = rawKey.replaceAll("\\[.*?]", ""); // remove [0], [1] to get 'service1'
                    String url = props.getProperty(name);

                    service.computeIfAbsent(serviceName, k->new ArrayList<>()).add(url);
                }
            }

            for(String currService : service.keySet()){
                pointer.put(currService, new AtomicInteger(0));
            }
            log.info("Loaded services: {}",service);
        }
        catch (IOException e){
            log.error("Error loading config.properties: {}",e.getMessage());
        }
    }

    public static String getNextUrl(String serviceName) {
        List<String> urls = service.get(serviceName);
        if (urls == null || urls.isEmpty()) {
            System.err.println("No URLs configured for service: " + serviceName);
            return null;
        }

        AtomicInteger counter = pointer.get(serviceName);
        int index = counter.getAndUpdate(i->(i+1)%urls.size());
        return urls.get(index);
    }

    public static Map<String, List<String>> getServiceMap() {
        return service;
    }
}
