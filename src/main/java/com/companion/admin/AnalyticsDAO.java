package com.companion.admin;

import com.companion.gokhul.DatabaseManager;
import com.companion.navis.CsvItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class AnalyticsDAO {

    public int getTotalUsers() {
        return getCount("SELECT COUNT(*) FROM users");
    }

    public int getTotalItems() {
        return getCount("SELECT COUNT(*) FROM items");
    }
    
    public int getTotalSearches() {
        return getCount("SELECT COUNT(*) FROM search_history");
    }

    public boolean logSearch(int userId, String queryText, String domain, int resultsCount) {
        String sql = "INSERT INTO search_history (user_id, query_text, domain, results_count) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, queryText);
            pstmt.setString(3, domain);
            pstmt.setInt(4, resultsCount);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addItemsBatch(List<CsvItem> items) {
        String sql = "INSERT INTO items (name, price, domain_category, rating, tags, description, sub_category, diet_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (CsvItem item : items) {
                    pstmt.setString(1, item.getName());
                    pstmt.setDouble(2, item.getPrice());
                    pstmt.setString(3, item.getDomainCategory());
                    pstmt.setDouble(4, item.getRating());
                    pstmt.setString(5, item.getTags());
                    pstmt.setString(6, item.getDescription());
                    pstmt.setString(7, item.getSubCategory());
                    pstmt.setString(8, item.getDietType());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public java.util.List<String> getRecentSearches(int userId) {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT query_text FROM search_history WHERE user_id = ? AND query_text != '' GROUP BY query_text ORDER BY MAX(searched_at) DESC LIMIT 3";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String q = rs.getString("query_text");
                    if (q != null && !q.trim().isEmpty() && !q.equalsIgnoreCase("Enter your query here...")) {
                        list.add(q);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
