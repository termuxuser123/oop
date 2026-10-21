package model;

public class TextBook extends LibraryItem {
    public TextBook(String isbn, String title, String author, int copies, int available, String ShelfLocation) {
        super(isbn, title, author, copies, available, ShelfLocation);
    }

    public boolean isIssuable() {
        return true;
    }

    public String getType() {
        return "TEXT BOOK";
    }
}
