package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import dao.impl.BookDAOImpl;
import dao.impl.UserDAOImpl;

public final class IssueTransaction extends Transaction {

    public IssueTransaction(User user, LibraryItem book, LocalDate issueDate) {
        super(user, book, issueDate);
    }

    @Override
    public void process() {
        process(getUser().getLoanPeriod());
    }

    public void process(int requestedDays) {
        if (!getBook().isIssuable()) {
            throw new IllegalStateException("item not issuable");
        }
        getBook().checkOutCopies();
        int days = getBook().getLoanPeriod(requestedDays);   // Journal still caps at 7
        setDueDate(getIssueDate().plusDays(days));
    }

    public static IssueTransaction fromResultSet(ResultSet rs) throws SQLException {
        User user = new UserDAOImpl().findById(rs.getInt("user_id"));
        LibraryItem book = new BookDAOImpl().findByIsbn(rs.getString("isbn"));
        IssueTransaction t = new IssueTransaction(user, book, rs.getDate("issue_date").toLocalDate());
        t.setDueDate(rs.getDate("due_date").toLocalDate());
        t.setTxnId(rs.getInt("txn_id"));
        return t;
    }
}