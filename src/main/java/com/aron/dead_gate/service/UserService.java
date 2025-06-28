package com.aron.dead_gate.service;

import com.aron.dead_gate.model.User;
import com.aron.dead_gate.repository.UserRepository;

import java.util.List;

public class UserService {
    private final UserRepository userRepo = new UserRepository();
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(int id) {
        return userRepo.findById(id);
    }
}
