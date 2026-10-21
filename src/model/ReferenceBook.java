package model;

public class ReferenceBook extends LibraryItem {
    public ReferenceBook(String isbn, String title, String author, int copies, int available, String ShelfLocation) {
        super(isbn, title, author, copies, available, ShelfLocation);
    }

    public boolean isIssuable() {
        return false;
    }

    public String getType() {
        return "REFERENCE BOOK";
    }
}
