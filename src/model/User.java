package model;

import org.mindrot.jbcrypt.BCrypt;

public abstract class User {
    private String name;
    private String email;
    private final int id;
    private String contactNo;
    private String passwordHash;
    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    protected User(String name, String email, int id, String contactNo, String password) {
        this.id = id;
        setName(name);
        setMail(email);
        setContactNo(contactNo);
        // DAOs pass the stored hash; registration passes a raw password
        if (password != null && password.matches(BCRYPT_PATTERN)) {
            this.passwordHash = password;
        } else {
            setPassword(password);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMail() {
        return email;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash required");
        }

        this.passwordHash = passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password required");
        }
        this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public final void setMail(String mail) {
        if (mail == null || !mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("invalid email");
        }
        this.email = mail.trim();
    }

    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name required");
        }
        this.name = name.trim();
    }

    public final void setContactNo(String contactNo) {
        if (contactNo == null || !contactNo.matches("\\d{7,15}")) {
            throw new IllegalArgumentException("contact must be 7-15 digits");
        }
        this.contactNo = contactNo;
    }

    public abstract int getMaxBookAllowed();

    public abstract int getLoanPeriod();

    public abstract String getRole();
}
