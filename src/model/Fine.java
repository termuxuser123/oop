package model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Fine {
    private int fineId;
    private final int txnId;
    private final BigDecimal amount;
    private boolean paid;

    public Fine(int txnId, BigDecimal amount, boolean paid) {
        this.txnId = txnId;
        this.amount = amount;
        this.paid = paid;
    }

    public int getFineId() { return fineId; }
    public void setFineId(int fineId) { this.fineId = fineId; }
    public int getTxnId() { return txnId; }
    public BigDecimal getAmount() { return amount; }
    public boolean isPaid() { return paid; }

    public static Fine fromResultSet(ResultSet rs) throws SQLException {
        Fine f = new Fine(rs.getInt("txn_id"), rs.getBigDecimal("amount"), rs.getBoolean("paid_status"));
        f.fineId = rs.getInt("fine_id");
        return f;
    }
}