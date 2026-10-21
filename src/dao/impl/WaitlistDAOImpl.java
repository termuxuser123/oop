package dao.impl;

import dao.WaitlistDAO;
import database.DBConnectionManager;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WaitlistDAOImpl implements WaitlistDAO {

    public void add(String isbn, User user) throws SQLException {
        try (Connection con = DBConnectionManager.getConnection()) {
            add(isbn,user, con);
        }
    }

    @Override
    public void add(String isbn, User user, Connection con) throws SQLException {
        String sql = "INSERT IGNORE INTO waitlist (isbn, user_id, request_date) VALUES (?,?,CURRENT_DATE())";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.setInt(2, user.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public User popNext(String isbn) throws SQLException {
        try (Connection con = DBConnectionManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                User next = popNext(isbn, con);
                con.commit();
                return next;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    @Override
    public User popNext(String isbn, Connection con) throws SQLException {
        String select = "SELECT u.* FROM waitlist w JOIN users u ON w.user_id = u.user_id "
                + "WHERE w.isbn = ? ORDER BY w.waitlist_id ASC LIMIT 1 FOR UPDATE";
        String delete = "DELETE FROM waitlist WHERE isbn = ? AND user_id = ?";

        User next = null;
        try (PreparedStatement ps = con.prepareStatement(select)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) next = buildUserFromResultSet(rs);
            }
        }
        if (next == null) return null;

        try (PreparedStatement del = con.prepareStatement(delete)) {
            del.setString(1, isbn);
            del.setInt(2, next.getId());
            del.executeUpdate();
        }
        return next;
    }

    @Override
    public void remove(String isbn, int userId) throws SQLException {
        String sql = "DELETE FROM waitlist WHERE isbn = ? AND user_id = ?";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public int size(String isbn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM waitlist WHERE isbn = ?";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public List<User> findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT u.* FROM waitlist w JOIN users u ON w.user_id = u.user_id "
               + "WHERE w.isbn = ? ORDER BY w.waitlist_id ASC";
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnectionManager.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildUserFromResultSet(rs));
                }
            }
        }
        return list;
    }

    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String email = rs.getString("email");
        int id = rs.getInt("user_id");
        String role = rs.getString("role");
        String contact = rs.getString("contact");
        String hash = rs.getString("password_hash");
        switch (role) {
            case "STUDENT":
                return new model.Student(name, email, id, contact, hash);
            case "FACULTY":
                return new model.Faculty(name, email, id, contact, hash);
            case "ADMIN":
                return new model.Admin(name, email, id, contact, hash);
            default:
                throw new SQLException("Unknown role: " + role);
        }
    }

    @Override
    public void removeAllForIsbn(String isbn) throws SQLException {
        String sql = "DELETE FROM waitlist WHERE isbn = ?";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.executeUpdate();
        }
    }
}
