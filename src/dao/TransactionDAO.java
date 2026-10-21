package dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.IssueTransaction;
import model.ReturnTransaction;
import java.sql.Connection;

public interface TransactionDAO {
    public void insertIssue(IssueTransaction txn) throws SQLException;
    public void insertIssue(IssueTransaction txn, Connection con) throws SQLException;
    
    public void insertReturn(ReturnTransaction txn) throws SQLException;
    public void insertReturn(ReturnTransaction txn, Connection con) throws SQLException;

    public List<IssueTransaction> findActiveByUser(int userId) throws SQLException;

    public IssueTransaction findById(int txnId) throws SQLException;
}
