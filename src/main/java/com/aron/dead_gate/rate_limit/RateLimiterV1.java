package com.aron.dead_gate.rate_limit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiterV1 {
    private final int maxRequestsPerSecond;
    private final Map<String,RequestCounter> counters = new ConcurrentHashMap<>();

    public RateLimiterV1(int maxRequestsPerSecond){
        this.maxRequestsPerSecond = maxRequestsPerSecond;
    }
    public boolean isAllowed(String key){
        long currTime = System.currentTimeMillis()/1000;

        RequestCounter counter = counters.computeIfAbsent(key,k->new RequestCounter());
        synchronized (counter){
            if(counter.second != currTime){
                counter.second = currTime;
                counter.count.set(1);
                return true;
            }
            else if(counter.count.get() < maxRequestsPerSecond){
                counter.count.incrementAndGet();
                return true;
            }
            return false;
        }
    }
    private static class RequestCounter {
        long second;
        AtomicInteger count = new AtomicInteger(0);
    }


}
