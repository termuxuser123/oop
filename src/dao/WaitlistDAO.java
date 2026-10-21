package dao;

import java.sql.SQLException;
import java.util.List;
import model.User;
import java.sql.Connection;

public interface WaitlistDAO {
    public void add(String isbn, User user) throws SQLException;
    public void add(String isbn, User user, Connection con) throws SQLException;

    public User popNext(String isbn) throws SQLException;
    public User popNext(String isbn, Connection con) throws SQLException;

    public void remove(String isbn, int userId) throws SQLException;

    public int size(String isbn) throws SQLException;

    public List<User> findByIsbn(String isbn) throws SQLException;

    public void removeAllForIsbn(String isbn) throws SQLException;

}
