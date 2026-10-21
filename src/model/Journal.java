package model;

public class Journal extends LibraryItem {
    public static final int MAX_LOAN_DAYS = 7;

    public Journal(String isbn, String title, String author, int copies, int available, String ShelfLocation) {
        super(isbn, title, author, copies, available, ShelfLocation);
    }

    public boolean isIssuable() {
        return true;
    }

    public int getLoanPeriod(int userLoanDays) {
        return Math.min(userLoanDays, MAX_LOAN_DAYS);
    }

    public String getType() {
        return "JOURNAL";
    }
}
