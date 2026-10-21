package dao.impl;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import model.LibraryItem;
import model.TextBook;
import model.ReferenceBook;
import model.Journal;
import database.DBConnectionManager;
import dao.BookDAO;

public class BookDAOImpl implements BookDAO {
    public void insert(LibraryItem book) throws SQLException {
        String query = "INSERT INTO books (isbn, title, author, type, total_copies, available_copies, shelf_location) VALUES(?,?,?,?,?,?,?)";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getType());
            pstmt.setInt(5, book.getTotalCopies());
            pstmt.setInt(6, book.getAvailableCopies());
            pstmt.setString(7, book.getShelfLocation());
            pstmt.executeUpdate();
        }
    }
    public void update(LibraryItem book) throws SQLException {
        try (Connection con = DBConnectionManager.getConnection()) {
            update(book, con);
        }
    }

    public void update(LibraryItem book, Connection con) throws SQLException {
        String query = "UPDATE books SET title=?, author=?,type=?, total_copies=?, available_copies=?,shelf_location=? WHERE isbn=?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getType());
            pstmt.setInt(4, book.getTotalCopies());
            pstmt.setInt(5, book.getAvailableCopies());
            pstmt.setString(6, book.getShelfLocation());
            pstmt.setString(7, book.getIsbn());
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No book found with ISBN: " + book.getIsbn());
            }
        }
    }

    public LibraryItem findByIsbn(String isbn) throws SQLException {
        String query = "SELECT * FROM books WHERE isbn=?";
        LibraryItem book = null;
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, isbn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    String type = rs.getString("type");
                    int total_copies = rs.getInt("total_copies");
                    int available_copies = rs.getInt("available_copies");
                    String shelf_location = rs.getString("shelf_location");
                    if (type.equals("REFERENCE BOOK")) {
                        book = new ReferenceBook(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else if (type.equals("TEXT BOOK")) {
                        book = new TextBook(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else if (type.equals("JOURNAL")) {
                        book = new Journal(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else {
                        throw new SQLException("Unknown book type: " + type);
                    }

                }
            }
            return book;
        }
    }

    public List<LibraryItem> findAll() throws SQLException {
        List<LibraryItem> all_books = new ArrayList<>();
        String query = "SELECT * FROM books";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement pstmt = con.prepareStatement(query)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String isbn = rs.getString("isbn");
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    String type = rs.getString("type");
                    int total_copies = rs.getInt("total_copies");
                    int available_copies = rs.getInt("available_copies");
                    String shelf_location = rs.getString("shelf_location");
                    LibraryItem book = null;
                    if (type.equals("REFERENCE BOOK")) {
                        book = new ReferenceBook(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else if (type.equals("TEXT BOOK")) {
                        book = new TextBook(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else if (type.equals("JOURNAL")) {
                        book = new Journal(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else {
                        throw new SQLException("Unknown book type: " + type);
                    }
                    all_books.add(book);
                }
            }
        }
        return all_books;
    }

    public void deleteByIsbn(String isbn) throws SQLException {
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ps.executeUpdate();
        }
    }

    @Override
    public List<LibraryItem> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ?";
        String like = "%" + keyword.toLowerCase() + "%";
        List<LibraryItem> list = new ArrayList<>();
        try (Connection con = DBConnectionManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
		            String isbn = rs.getString("isbn");
                    String title = rs.getString("title");
                    String author = rs.getString("author");
                    String type = rs.getString("type");
                    int total_copies = rs.getInt("total_copies");
                    int available_copies = rs.getInt("available_copies");
                    String shelf_location = rs.getString("shelf_location");
                    LibraryItem book = null;
                    if (type.equals("REFERENCE BOOK")) {
                        book = new ReferenceBook(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else if (type.equals("TEXT BOOK")) {
                        book = new TextBook(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else if (type.equals("JOURNAL")) {
                        book = new Journal(isbn, title, author, total_copies, available_copies, shelf_location);
                    } else {
                        throw new SQLException("Unknown book type: " + type);
                    }
                    list.add(book);
                }
            }
        }
        return list;
    }

}
