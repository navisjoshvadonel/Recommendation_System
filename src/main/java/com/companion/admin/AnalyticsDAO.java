package com.companion.admin;

import com.companion.gokhul.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AnalyticsDAO {

    public int getTotalUsers() {
        return getCount("SELECT COUNT(*) FROM users");
    }

    public int getTotalItems() {
        return getCount("SELECT COUNT(*) FROM items");
    }
    
    // In a full implementation we'd also track search history
    public int getTotalSearches() {
        return getCount("SELECT COUNT(*) FROM search_history");
    }

    public boolean addItem(String name, double price, String category, double rating, String tags, String desc, String subCat, String diet) {
        String sql = "INSERT INTO items (name, price, domain_category, rating, tags, description, sub_category, diet_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setString(3, category);
            pstmt.setDouble(4, rating);
            pstmt.setString(5, tags);
            pstmt.setString(6, desc);
            pstmt.setString(7, subCat);
            pstmt.setString(8, diet);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getCount(String sql) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
