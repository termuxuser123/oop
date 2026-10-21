package service;

import java.util.List;
import model.LibraryItem;

public interface Searchable {
    // search about the vook by id,title,author,IBSN
    List<LibraryItem> search(String keyword);

    // to get the physical location
    String getShelfLocation(String isbn);
}