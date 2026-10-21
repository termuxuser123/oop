package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class Transaction {
    private int txnId;                 // set by the DB after insert (0 until then)
    private final User user;
    private final LibraryItem book;
    private final LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    protected Transaction(User user, LibraryItem book, LocalDate issueDate) {
        if (user == null || book == null || issueDate == null)
            throw new IllegalArgumentException("user, book and issueDate are required");
        this.user = user;
        this.book = book;
        this.issueDate = issueDate;
    }

    public int getTxnId() {
        return txnId;
    }

    public void setTxnId(int txnId) {      // public: called from dao.impl
        this.txnId = txnId;
    }

    public User getUser() {
        return user;
    }

    public LibraryItem getBook() {
        return book;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    protected final void setDueDate(LocalDate d) {
        this.dueDate = d;
    }

    protected final void setReturnDate(LocalDate d) {
        this.returnDate = d;
    }

    public int getOverdueDays() {
        if (returnDate == null || dueDate == null) {
            return 0;
        }
        return (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
    }

    public abstract void process();
}