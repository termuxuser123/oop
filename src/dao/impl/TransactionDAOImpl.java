package dao.impl;

import dao.TransactionDAO;
import database.DBConnectionManager;
import model.IssueTransaction;
import model.ReturnTransaction;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    public void insertIssue(IssueTransaction txn) throws SQLException {
        try (Connection con = DBConnectionManager.getConnection()) {
            insertIssue(txn, con);
        }
    }
    @Override
    public void insertIssue(IssueTransaction txn, Connection con) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, isbn, issue_date, due_date, status) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, txn.getUser().getId());
            ps.setString(2, txn.getBook().getIsbn());
            ps.setDate(3, Date.valueOf(txn.getIssueDate()));
            ps.setDate(4, Date.valueOf(txn.getDueDate()));
            ps.setString(5, "ISSUED");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    txn.setTxnId(rs.getInt(1));
            }
        }
    }

    public void insertReturn(ReturnTransaction txn) throws SQLException {
        try (Connection con = DBConnectionManager.getConnection()) {
            insertReturn(txn,con);
        }
    }
    @Override
    public void insertReturn(ReturnTransaction txn, Connection con) throws SQLException {
        String sql = "UPDATE transactions SET return_date = ?, status = ? WHERE txn_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(txn.getReturnDate()));
            ps.setString(2, "RETURNED");
            ps.setInt(3, txn.getIssueTxn().getTxnId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<IssueTransaction> findActiveByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND status = 'ISSUED'";
        List<IssueTransaction> list = new ArrayList<>();
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(IssueTransaction.fromResultSet(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<IssueTransaction> findAllActive() throws SQLException {
        String sql = "SELECT * FROM transactions WHERE status = 'ISSUED'";
        List<IssueTransaction> list = new ArrayList<>();
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(IssueTransaction.fromResultSet(rs));
            }
        }
        return list;
    }

    @Override
    public IssueTransaction findById(int txnId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE txn_id = ?";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return IssueTransaction.fromResultSet(rs);
                return null;
            }
        }
    }
}
