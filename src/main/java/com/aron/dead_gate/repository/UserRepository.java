package com.aron.dead_gate.repository;

import com.aron.dead_gate.db.DBConnection;
import com.aron.dead_gate.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class UserRepository {
    public List<User> findAll(){
        List<User> users = new LinkedList<>();
        try(Connection conn = DBConnection.getConn()){
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                users.add(new User(rs.getInt("id"),rs.getString("name")));
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return users;
    }

    public User findById(int id) {
        try(Connection conn = DBConnection.getConn()){
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
