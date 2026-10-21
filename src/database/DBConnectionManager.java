package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionManager{
    public static Connection getConnection() throws SQLException{
        String url="jdbc:mysql://localhost:3306/library_db";
        String user=System.getenv("LIBRARY_DB_USER");
        String pass=System.getenv("LIBRARY_DB_PASSWORD");
        if (user == null || pass == null) {
            throw new SQLException("Set LIBRARY_DB_USER and LIBRARY_DB_PASSWORD environment variables.");
        }
        Connection con=DriverManager.getConnection(url,user,pass);
        return con;
        }
    }
