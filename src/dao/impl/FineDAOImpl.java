package dao.impl;

import dao.FineDAO;
import database.DBConnectionManager;
import model.Fine;
import java.sql.*;

public class FineDAOImpl implements FineDAO {

    public void insert(Fine fine) throws SQLException {
        try (Connection con = DBConnectionManager.getConnection()) {
            insert(fine, con);
        }
    }  

    @Override
    public void insert(Fine fine, Connection con) throws SQLException{
        String sql = "INSERT INTO fines (txn_id, amount, paid_status) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fine.getTxnId());
            ps.setBigDecimal(2, fine.getAmount());
            ps.setBoolean(3, fine.isPaid());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    fine.setFineId(rs.getInt(1));
            }
        }
    }

    @Override
    public Fine findByTxnId(int txnId) throws SQLException {
        String sql = "SELECT * FROM fines WHERE txn_id = ?";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Fine.fromResultSet(rs);
                return null;
            }
        }
    }

    @Override
    public void markPaid(int fineId) throws SQLException {
        String sql = "UPDATE fines SET paid_status = TRUE WHERE fine_id = ?";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, fineId);
            ps.executeUpdate();
        }
    }
}
