package model;

public class Student extends User {
    public static final int MAX_BOOKS = 3;
    public static final int LOAN_DAYS = 14;

    public Student(String name, String email, int id, String contactNo, String password) {
        super(name, email, id, contactNo, password);
    }

    public int getMaxBookAllowed() {
        return MAX_BOOKS;
    }

    public int getLoanPeriod() {
        return LOAN_DAYS;
    }

    public String getRole() {
        return "STUDENT";
    }

}
