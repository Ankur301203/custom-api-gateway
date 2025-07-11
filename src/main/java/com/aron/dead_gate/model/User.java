package com.aron.dead_gate.model;

import jakarta.persistence.Id;

public class User {
    @Id
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
        return String.format("{\"id\":%d,\"name\":\"%s\"}", id, name);
    }


}
