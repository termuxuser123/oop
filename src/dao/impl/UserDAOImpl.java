package dao.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import database.DBConnectionManager;
import dao.UserDAO;
import model.User;
import model.Student;
import model.Admin;
import model.Faculty;

import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

public class UserDAOImpl implements UserDAO {
    public void insert(User user) throws SQLException {
        String queryWithStatus = "INSERT INTO users (name, email, role, contact, password_hash, status) VALUES(?,?,?,?,?,'PENDING')";
        String queryWithoutStatus = "INSERT INTO users (name, email, role, contact, password_hash) VALUES(?,?,?,?,?)";
        try (Connection con = DBConnectionManager.getConnection()) {
            try (PreparedStatement pstmt = con.prepareStatement(queryWithStatus)) {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getMail());
                pstmt.setString(3, user.getRole());
                pstmt.setString(4, user.getContactNo());
                pstmt.setString(5, user.getPasswordHash());
                pstmt.executeUpdate();
                return;
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("status")) {
                    try (PreparedStatement pstmt = con.prepareStatement(queryWithoutStatus)) {
                        pstmt.setString(1, user.getName());
                        pstmt.setString(2, user.getMail());
                        pstmt.setString(3, user.getRole());
                        pstmt.setString(4, user.getContactNo());
                        pstmt.setString(5, user.getPasswordHash());
                        pstmt.executeUpdate();
                        return;
                    }
                }
                throw e;
            }
        }
    }

    public User authenticate(String email, String pass) throws SQLException {
        String query = "SELECT * FROM users WHERE email=?";
        User user = null;
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String resultEmail = rs.getString("email");
                    String password = rs.getString("password_hash");

                    // safe password check (handles BCrypt hash and fallback plain text)
                    boolean ok = false;
                    try {
                        if (password != null && password.startsWith("$2") && password.length() >= 59) {
                            ok = BCrypt.checkpw(pass, password);
                        } else {
                            ok = pass.equals(password);
                        }
                    } catch (Exception e) {
                        ok = pass.equals(password);
                    }

                    if (!ok) {
                        return null;
                    }

                    String role = rs.getString("role");

                    // check approval status if column exists and not admin
                    String status = null;
                    try {
                        status = rs.getString("status");
                    } catch (SQLException ignored) {
                        // older database schema without status column
                    }

                    if (!"ADMIN".equalsIgnoreCase(role)) {
                        if ("PENDING".equalsIgnoreCase(status)) {
                            throw new SQLException("Your registration is awaiting admin approval.");
                        }
                        if ("REJECTED".equalsIgnoreCase(status)) {
                            throw new SQLException("Your registration was not approved. Please contact the library admin.");
                        }
                    }

                    String name = rs.getString("name");
                    int id = rs.getInt("user_id");
                    String contact = rs.getString("contact");
                    if (contact == null || !contact.matches("\\d{7,15}")) {
                        contact = "9876543210";
                    }
                    if ("STUDENT".equalsIgnoreCase(role)) {
                        user = new Student(name, resultEmail, id, contact, password);
                    } else if ("ADMIN".equalsIgnoreCase(role)) {
                        user = new Admin(name, resultEmail, id, contact, password);
                    } else if ("FACULTY".equalsIgnoreCase(role)) {
                        user = new Faculty(name, resultEmail, id, contact, password);
                    } else {
                        throw new SQLException("Unknown user role: " + role);
                    }
                } else {
                    return null;
                }
            }
            return user;
        }
    }

    public User findById(int id) throws SQLException {
        String query = "SELECT * FROM users WHERE user_id=?";
        User user = null;
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    String contact = rs.getString("contact");
                    if (contact == null || !contact.matches("\\d{7,15}")) {
                        contact = "9876543210";
                    }
                    String password = rs.getString("password_hash");
                    if (role.equals("STUDENT")) {
                        user = new Student(name, email, id, contact, password);
                    } else if (role.equals("FACULTY")) {
                        user = new Faculty(name, email, id, contact, password);
                    } else if (role.equals("ADMIN")) {
                        user = new Admin(name, email, id, contact, password);
                    } else {
                        throw new SQLException("Unknown user role: " + role);
                    }
                    user.setPasswordHash(password);
                }
            }
            return user;
        }
    }

	@Override
	public void deleteById(int userId) throws SQLException {
		String fineCheck = "SELECT COUNT(*) FROM fines f "
				+ "JOIN transactions t ON f.txn_id = t.txn_id "
				+ "WHERE t.user_id = ? AND f.paid_status = FALSE";
		String txnCheck = "SELECT COUNT(*) FROM transactions "
				+ "WHERE user_id = ? AND status = 'ISSUED'";

		String deleteFines = "DELETE f FROM fines f "
				+ "JOIN transactions t ON f.txn_id = t.txn_id "
				+ "WHERE t.user_id = ?";
		String deleteTxns = "DELETE FROM transactions WHERE user_id = ?";
		String deleteWaitlist = "DELETE FROM waitlist WHERE user_id = ?";
		String deleteUser = "DELETE FROM users WHERE user_id = ?";

		try (Connection con = DBConnectionManager.getConnection()) {
			con.setAutoCommit(false);
			try {
				try (PreparedStatement psFine = con.prepareStatement(fineCheck);
						PreparedStatement psTxn = con.prepareStatement(txnCheck)) {
					psFine.setInt(1, userId);
					psTxn.setInt(1, userId);
					try (ResultSet rsFine = psFine.executeQuery();
							ResultSet rsTxn = psTxn.executeQuery()) {
						rsFine.next();
						rsTxn.next();
						if (rsFine.getInt(1) > 0) {
							throw new SQLException("Cannot delete user: unpaid fines exist.");
						}
						if (rsTxn.getInt(1) > 0) {
							throw new SQLException("Cannot delete user: active transactions exist.");
						}
					}
				}

				try (PreparedStatement p1 = con.prepareStatement(deleteFines);
						PreparedStatement p2 = con.prepareStatement(deleteTxns);
						PreparedStatement p3 = con.prepareStatement(deleteWaitlist);
						PreparedStatement p4 = con.prepareStatement(deleteUser)) {
					p1.setInt(1, userId); p1.executeUpdate();
					p2.setInt(1, userId); p2.executeUpdate();
					p3.setInt(1, userId); p3.executeUpdate();
					p4.setInt(1, userId); p4.executeUpdate();
				}
				con.commit();
			} catch (SQLException e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, role = ?, contact = ?, password_hash = ? WHERE user_id = ?";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getMail());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getContactNo());
            ps.setString(5, user.getPasswordHash());
            ps.setInt(6, user.getId());
            ps.executeUpdate();
        }
    }
    
    public List<User> getPendingUsers() throws SQLException {
        String query = "SELECT * FROM users WHERE status='PENDING' ORDER BY user_id";
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnectionManager.getConnection()) {
            try (PreparedStatement pstmt = con.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    String contact = rs.getString("contact");
                    String password = rs.getString("password_hash");
                    if (role.equals("FACULTY")) {
                        list.add(new Faculty(name, email, id, contact, password));
                    } else {
                        list.add(new Student(name, email, id, contact, password));
                    }
                }
            } catch (SQLException ex) {
                if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("status")) {
                    // Try to auto-create the status column if possible
                    try (Statement stmt = con.createStatement()) {
                        stmt.executeUpdate("ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED'");
                    } catch (SQLException ignored) {
                    }
                    return list;
                }
                throw ex;
            }
        }
        return list;
    }

    public void updateStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE users SET status=? WHERE user_id=? AND status='PENDING'";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("status")) {
                return;
            }
            throw ex;
        }
    }
}
