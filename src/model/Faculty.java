package model;

public class Faculty extends User {
    public static final int MAX_BOOKS = 8;
    public static final int LOAN_DAYS = 30;

    public Faculty(String name, String email, int id, String contactNo, String password) {
        super(name, email, id, contactNo, password);
    }

    public int getMaxBookAllowed() {
        return MAX_BOOKS;
    }

    public int getLoanPeriod() {
        return LOAN_DAYS;
    }

    public String getRole() {
        return "FACULTY";
    }

}
