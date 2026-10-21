package dao;

import java.sql.SQLException;
import model.Fine;
import java.sql.Connection;

public interface FineDAO {
    public void insert(Fine fine) throws SQLException;
    public void insert(Fine fine, Connection con) throws SQLException;

    public Fine findByTxnId(int txnId) throws SQLException;

    public void markPaid(int fineId) throws SQLException;
}
