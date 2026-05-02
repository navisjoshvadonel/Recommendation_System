package com.companion.auth;

import com.companion.gokhul.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class UserDAO {

    public enum RegisterResult {
        SUCCESS,
        DUPLICATE_USERNAME,
        DUPLICATE_EMAIL,
        ERROR
    }

    public RegisterResult register(String username, String email, String password, String budgetPref, String dietPref, String role) {
        if (username == null || username.trim().isEmpty()) return RegisterResult.ERROR;
        if (email == null || email.trim().isEmpty()) return RegisterResult.ERROR;
        if (password == null || password.isEmpty()) return RegisterResult.ERROR;

        String salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(password, salt);

        String sql = "INSERT INTO users (username, email, password_hash, salt, budget_pref, diet_pref, role) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, email.trim());
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, salt);
            pstmt.setString(5, budgetPref);
            pstmt.setString(6, dietPref);
            pstmt.setString(7, role);

            pstmt.executeUpdate();
            return RegisterResult.SUCCESS;

        } catch (SQLIntegrityConstraintViolationException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("username")) return RegisterResult.DUPLICATE_USERNAME;
            if (msg.contains("email")) return RegisterResult.DUPLICATE_EMAIL;
            return RegisterResult.DUPLICATE_USERNAME;
        } catch (SQLException e) {
            e.printStackTrace();
            return RegisterResult.ERROR;
        }
    }

    public UserRecord login(String username, String password) {
        if (username == null || username.trim().isEmpty()) return null;
        String sql = "SELECT id, username, email, password_hash, salt, diet_pref, budget_pref, role FROM users WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String storedSalt = rs.getString("salt");

                if (PasswordUtils.verifyPassword(password, storedHash, storedSalt)) {
                    UserRecord user = new UserRecord();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setDietPref(rs.getString("diet_pref"));
                    user.setBudgetPref(rs.getString("budget_pref"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
