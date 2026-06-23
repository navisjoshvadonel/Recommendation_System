package com.companion.auth;

import com.companion.gokhul.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InteractionDAO {

    public boolean logInteraction(int userId, int itemId, String interactionType) {
        String sql = "INSERT INTO user_interactions (user_id, item_id, interaction_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setString(3, interactionType);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            // Might throw unique constraint violation if already interacted
            // Safe to ignore for 'like' since it's already recorded
            return false;
        }
    }

    public boolean removeInteraction(int userId, int itemId, String interactionType) {
        String sql = "DELETE FROM user_interactions WHERE user_id = ? AND item_id = ? AND interaction_type = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setString(3, interactionType);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Set<Integer> getLikedItems(int userId) {
        Set<Integer> likedItems = new HashSet<>();
        String sql = "SELECT item_id FROM user_interactions WHERE user_id = ? AND interaction_type = 'like'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                likedItems.add(rs.getInt("item_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return likedItems;
    }

    public Map<String, Double> getUserDomainWeights(int userId) {
        Map<String, Double> domainWeights = new HashMap<>();
        String sql = "SELECT i.domain_category, COUNT(*) as count FROM user_interactions ui " +
                     "JOIN items i ON ui.item_id = i.id " +
                     "WHERE ui.user_id = ? " +
                     "GROUP BY i.domain_category";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String domain = rs.getString("domain_category");
                int count = rs.getInt("count");
                // Base weight addition per interaction
                domainWeights.put(domain, count * 5.0); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domainWeights;
    }
}
