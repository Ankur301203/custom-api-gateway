package com.aron.dead_gate.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class User {
    private int id;
    private String name;
    
    public User(){}

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        }
        catch (IOException e) {
            throw new RuntimeException("Error converting user to JSON", e);
        }
    }

}
