package service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import dao.BookDAO;
import dao.WaitlistDAO;
import dao.TransactionDAO;
import dao.FineDAO;
import dao.impl.BookDAOImpl;
import dao.impl.WaitlistDAOImpl;
import dao.impl.TransactionDAOImpl;
import dao.impl.FineDAOImpl;
import database.DBConnectionManager;
import model.*;

public class Library implements Borrowable, Searchable, Reportable {

    // DAOs
    private final BookDAO bookDAO = new BookDAOImpl();
    private final WaitlistDAO waitlistDAO = new WaitlistDAOImpl();
    private final TransactionDAO txnDAO = new TransactionDAOImpl();
    private final FineDAO fineDAO = new FineDAOImpl();

    private final List<LibraryItem> inventory = new ArrayList<>();
    private final Map<String, IssueTransaction> activeLoans = new HashMap<>();
    private final Map<Integer, Integer> userLoanCounts = new HashMap<>();
    private final FinePolicy standardPolicy = new StandardFinePolicy();
    private final FinePolicy facultyPolicy = new FacultyFinePolicy();
    private final WaitlistManager waitlistManager = new WaitlistManager();

    public Library() {
        try {
            this.inventory.addAll(bookDAO.findAll());
        } catch (SQLException e) {
            System.err.println("[LIBRARY INIT] Could not load books: " + e.getMessage());
        }
    }


    public void addItem(LibraryItem item) {
        try {
            bookDAO.insert(item);
        } catch (SQLException e) {
            System.err.println("[LIBRARY] Failed to persist new book: " + e.getMessage());
            return; // don't leave a ghost book in memory
        }
        inventory.add(item);
        System.out.println("[CATALOG] Added: " + item.getTitle() + " (ISBN: " + item.getIsbn() + ")");
    }

    public void removeItem(String isbn) throws SQLException {
        LibraryItem item = findInInventory(isbn);
        if (item != null && item.getAvailableCopies() < item.getTotalCopies()) {
            System.out.println("[REMOVE ERROR] '" + item.getTitle() + "' still has copies on loan.");
            return;
        }
        waitlistDAO.removeAllForIsbn(isbn);   // waitlist rows reference the book, so first
        bookDAO.deleteByIsbn(isbn);
        inventory.removeIf(b -> b.getIsbn().equals(isbn));
    }

    private LibraryItem findInInventory(String isbn) {
        for (LibraryItem b : inventory) {
            if (b.getIsbn().equals(isbn)) return b;
        }
        return null;
    }

    private String getLoanKey(User user, LibraryItem item) {
        return user.getId() + "_" + item.getIsbn();
    }

    // ---------- issue ----------

    @Override
    public void issue(User user, LibraryItem item) {
        doIssue(user, item, null);
    }

    @Override
    public void issue(User user, LibraryItem item, int customDays) {
        if (customDays <= 0) {
            System.out.println("[ISSUE ERROR] Custom loan period must be at least 1 day.");
            return;
        }
        System.out.println("[CUSTOM LOAN] Issuing with custom period: " + customDays + " days.");
        doIssue(user, item, customDays);
    }

    private void doIssue(User user, LibraryItem requested, Integer customDays) {
        // always work on the inventory object so memory and DB stay in sync
        LibraryItem item = findInInventory(requested.getIsbn());
        if (item == null) {
            System.out.println("[ISSUE ERROR] Item (ISBN: " + requested.getIsbn() + ") is not in the catalog.");
            return;
        }

        if (!item.isIssuable()) {
            System.out.println("[ISSUE ERROR] '" + item.getTitle() + "' is a reference item and cannot be borrowed!");
            return;
        }

        String loanKey = getLoanKey(user, item);
        if (activeLoans.containsKey(loanKey)) {
            System.out.println("[ISSUE ERROR] " + user.getName() + " already has '" + item.getTitle() + "'.");
            return;
        }

        int currentLoans = userLoanCounts.getOrDefault(user.getId(), 0);
        if (currentLoans >= user.getMaxBookAllowed()) {
            System.out.println("[ISSUE ERROR] " + user.getName() + " reached max borrowing limit ("
                    + user.getMaxBookAllowed() + " books).");
            return;
        }

        if (!item.hasAvailableCopy()) {
            System.out.println("[OUT OF STOCK] No copies of '" + item.getTitle() + "' available.");
            waitlistManager.addToWaitlist(item.getIsbn(), user);
            return;
        }

        IssueTransaction txn = new IssueTransaction(user, item, LocalDate.now());
        if (customDays == null) {
            txn.process();
        } else {
            txn.process(customDays);
        }

        try {
            persistIssue(txn, item);
        } catch (SQLException e) {
            item.checkInCopies();   // undo the in-memory decrement
            System.out.println("[ISSUE ERROR] Could not save the loan: " + e.getMessage());
            return;
        }

        activeLoans.put(loanKey, txn);
        userLoanCounts.put(user.getId(), currentLoans + 1);

        System.out.println("[ISSUE SUCCESS] Issued '" + item.getTitle() + "' to " + user.getName()
                + " | Due Date: " + txn.getDueDate());
    }

    private void persistIssue(IssueTransaction txn, LibraryItem item) throws SQLException {
        try (Connection con = DBConnectionManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                txnDAO.insertIssue(txn, con);   // also sets the DB id on txn
                bookDAO.update(item, con);      // writes the decremented available_copies
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }


    @Override
    public void returnItem(User user, LibraryItem requested) {
        LibraryItem item = findInInventory(requested.getIsbn());
        if (item == null) {
            System.out.println("[RETURN ERROR] Item (ISBN: " + requested.getIsbn() + ") is not in the catalog.");
            return;
        }

        String loanKey = getLoanKey(user, item);
        IssueTransaction issueTxn = activeLoans.get(loanKey);
        if (issueTxn == null) {
            System.out.println("[RETURN ERROR] No active loan found for " + user.getName()
                    + " and '" + item.getTitle() + "'.");
            return;
        }

        ReturnTransaction returnTxn = new ReturnTransaction(issueTxn, LocalDate.now());
        try {
            returnTxn.process();
        } catch (IllegalStateException e) {
            System.out.println("[RETURN ERROR] " + e.getMessage());
            return;
        }

        // build the fine (only if the amount is above 0, e.g. faculty grace period)
        int overdueDays = returnTxn.getOverdueDays();
        double fineAmount = 0;
        FinePolicy policy = null;
        Fine fineRecord = null;
        if (overdueDays > 0) {
            policy = user.getRole().equalsIgnoreCase("FACULTY") ? facultyPolicy : standardPolicy;
            fineAmount = policy.calculateFine(overdueDays);
            if (fineAmount > 0) {
                fineRecord = new Fine(issueTxn.getTxnId(), BigDecimal.valueOf(fineAmount), false);
            }
        }

        try {
            persistReturn(returnTxn, item, fineRecord);
        } catch (SQLException e) {
            item.checkOutCopies();  // undo the in-memory increment
            System.out.println("[RETURN ERROR] Could not save the return: " + e.getMessage());
            return;
        }

        activeLoans.remove(loanKey);
        userLoanCounts.put(user.getId(), Math.max(0, userLoanCounts.getOrDefault(user.getId(), 1) - 1));

        System.out.println("[RETURN SUCCESS] '" + item.getTitle() + "' returned by " + user.getName());

        if (fineRecord != null) {
            System.out.println("[FINE APPLIED] " + overdueDays + " days overdue. Total Fine: ₹" + fineAmount
                    + " (Applied Policy: " + policy.getClass().getSimpleName() + ")");
        } else {
            System.out.println("[NO FINE] Book returned on time, early, or within the grace period.");
        }

        waitlistManager.notifyNextuser(item.getIsbn());   // after the commit
    }

    private void persistReturn(ReturnTransaction returnTxn, LibraryItem item, Fine fine) throws SQLException {
        try (Connection con = DBConnectionManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                txnDAO.insertReturn(returnTxn, con);
                bookDAO.update(item, con);      // incremented available_copies
                if (fine != null) {
                    fineDAO.insert(fine, con);
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


    @Override
    public List<LibraryItem> search(String keyword) {
        List<LibraryItem> results = new ArrayList<>();
        String query = keyword.toLowerCase();

        for (LibraryItem item : inventory) {
            if (item.getTitle().toLowerCase().contains(query)
                    || item.getAuthor().toLowerCase().contains(query)
                    || item.getIsbn().toLowerCase().contains(query)) {
                results.add(item);
            }
        }
        return results;
    }

    @Override
    public String getShelfLocation(String isbn) {
        LibraryItem item = findInInventory(isbn);
        return (item != null) ? item.getShelfLocation() : "Item not found";
    }

    @Override
    public String generateSummary() {
        return String.format("=== Library Summary ===\nTotal Catalog Items: %d\nActive Loans: %d",
                inventory.size(), activeLoans.size());
    }
}