package dao;

import java.util.List;
import java.sql.SQLException;
import model.User;
import java.sql.Connection;

public interface UserDAO {
    public void insert(User user) throws SQLException;

    public User authenticate(String email, String pass) throws SQLException;

    public User findById(int id) throws SQLException;

    public void deleteById(int id) throws SQLException;

    public void update(User user) throws SQLException;
}