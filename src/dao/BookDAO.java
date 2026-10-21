package dao;

import java.util.List;
import java.sql.SQLException;
import model.LibraryItem;
import java.sql.Connection;

public interface BookDAO {
    public void insert(LibraryItem book) throws SQLException;

    public void update(LibraryItem book) throws SQLException;
    public void update(LibraryItem book, Connection con) throws SQLException;

    public LibraryItem findByIsbn(String isbn) throws SQLException;

    public List<LibraryItem> findAll() throws SQLException;

    public void deleteByIsbn(String isbn) throws SQLException;

    public List<LibraryItem> search(String keyword) throws SQLException;
}