package com.aron.dead_gate.service;

import com.aron.dead_gate.model.User;
import com.aron.dead_gate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(int id) {
        return userRepo.findById(id).orElse(null);
    }

    public void createUser(User user) {
        userRepo.save(user);
    }

    public boolean updateUser(Integer id, User updatedUser) {
        User existingUser = userRepo.findById(id).orElse(null);
        if(existingUser == null){
            return false;
        }
        existingUser.setName(updatedUser.getName());
        userRepo.save(existingUser);
        return true;
    }

    public boolean deleteUser(Integer id) {
        User user = userRepo.findById(id).orElse(null);
        if(user == null){
            return false;
        }
        userRepo.delete(user);
        return true;
    }
}