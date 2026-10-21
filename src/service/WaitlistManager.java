package service;

import java.sql.SQLException;
import dao.WaitlistDAO;
import dao.impl.WaitlistDAOImpl;
import model.User;

public class WaitlistManager {
    private final WaitlistDAO waitlistDAO = new WaitlistDAOImpl();

    // add user to the waitlist for a book
    public void addToWaitlist(String isbn, User user) {
        try {
            for (User u : waitlistDAO.findByIsbn(isbn)) {
                if (u.getId() == user.getId()) {
                    System.out.println("[WAITLIST] " + user.getName()
                            + " is already in the waitlist for ISBN: " + isbn);
                    return;
                }
            }
            waitlistDAO.add(isbn, user);
            System.out.println("[WAITLIST] " + user.getName() + " added to waitlist for ISBN: "
                    + isbn + " (Position: " + waitlistDAO.size(isbn) + ")");
        } catch (SQLException e) {
            System.out.println("[WAITLIST ERROR] Could not update waitlist: " + e.getMessage());
        }
    }

    // when a book is returned, pop and return the next user (null if none)
    public User notifyNextuser(String isbn) {
        try {
            User nextUser = waitlistDAO.popNext(isbn);
            if (nextUser != null) {
                System.out.println("[WAITLIST NOTIFICATION] A copy of book (ISBN: " + isbn
                        + ") is now reserved for: " + nextUser.getName()
                        + " (" + nextUser.getMail() + ")");
            }
            return nextUser;
        } catch (SQLException e) {
            System.out.println("[WAITLIST ERROR] Could not read waitlist: " + e.getMessage());
            return null;
        }
    }

    public int getWaitlistSize(String isbn) {
        try {
            return waitlistDAO.size(isbn);
        } catch (SQLException e) {
            System.out.println("[WAITLIST ERROR] " + e.getMessage());
            return 0;
        }
    }
}