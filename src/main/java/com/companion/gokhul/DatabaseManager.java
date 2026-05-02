package com.companion.gokhul;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private String url;
    private String user;
    private String password;

    private DatabaseManager() {
        loadConfig();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            prop.load(input);
            this.url = prop.getProperty("db.url");
            this.user = prop.getProperty("db.username");
            this.password = prop.getProperty("db.password");
            
            if (this.password == null) {
                this.password = "navis2005"; 
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
