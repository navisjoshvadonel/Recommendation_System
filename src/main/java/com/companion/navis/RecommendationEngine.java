package com.companion.navis;

import com.companion.gokhul.DatabaseManager;
import com.companion.gokhul.Item;
import com.companion.gokhul.QueryAlgorithm;
import com.companion.gokhul.UserPreferences;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommendationEngine {

    public List<ScoredResult> getRecommendations(UserPreferences prefs) {
        List<ScoredResult> results = new ArrayList<>();
        
        // Algorithm: Query Parsing & Semantic Extraction
        QueryAlgorithm.parseQuery(prefs);
        
        String query = prefs.getSearchQuery().toLowerCase();
        List<String> keywords = QueryAlgorithm.extractKeywords(query);
        String sql = QueryAlgorithm.buildSqlQuery(prefs, keywords);

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            
            // 1. Domain Parameter
            String domain = prefs.getDomain();
            if (domain == null || domain.equalsIgnoreCase("All")) {
                domain = prefs.getDetectedDomain();
            }
            if (domain != null && !domain.equalsIgnoreCase("All") && !domain.isEmpty()) {
                pstmt.setString(paramIndex++, domain);
            }

            // 2. Price Parameters
            if (prefs.getMinPrice() != null) {
                pstmt.setDouble(paramIndex++, prefs.getMinPrice());
            }
            if (prefs.getMaxPrice() != null) {
                pstmt.setDouble(paramIndex++, prefs.getMaxPrice());
            }

            // 3. Diet Parameter
            if (prefs.getDiet() != null && !prefs.getDiet().equalsIgnoreCase("Any") && !prefs.getDiet().isEmpty()) {
                pstmt.setString(paramIndex++, prefs.getDiet());
            }

            // 4. Keyword Parameters
            if (!keywords.isEmpty()) {
                for (String word : keywords) {
                    String likeParam = "%" + word + "%";
                    pstmt.setString(paramIndex++, likeParam);
                    pstmt.setString(paramIndex++, likeParam);
                    pstmt.setString(paramIndex++, likeParam);
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Item item = mapItem(rs);
                double score = calculateScore(item, prefs, keywords);
                results.add(new ScoredResult(item, score));
            }

            // Fallback Algorithm: Fill remaining slots but STILL respect price constraints if present
            if (results.size() < 10) {
                StringBuilder fallbackSql = new StringBuilder("SELECT * FROM items WHERE domain_category = ? ");
                if (prefs.getMinPrice() != null) fallbackSql.append(" AND price >= ? ");
                if (prefs.getMaxPrice() != null) fallbackSql.append(" AND price <= ? ");
                fallbackSql.append(" AND id NOT IN (" + buildIdList(results) + ") ORDER BY rating DESC LIMIT 10");

                try (PreparedStatement fallbackPstmt = conn.prepareStatement(fallbackSql.toString())) {
                    int fbIdx = 1;
                    fallbackPstmt.setString(fbIdx++, domain != null ? domain : "Shopping & products");
                    if (prefs.getMinPrice() != null) fallbackPstmt.setDouble(fbIdx++, prefs.getMinPrice());
                    if (prefs.getMaxPrice() != null) fallbackPstmt.setDouble(fbIdx++, prefs.getMaxPrice());

                    ResultSet frs = fallbackPstmt.executeQuery();
                    while (frs.next()) {
                        results.add(new ScoredResult(mapItem(frs), 5.0)); 
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(results);
        return results;
    }

    private Item mapItem(ResultSet rs) throws Exception {
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setPrice(rs.getDouble("price"));
        item.setDomainCategory(rs.getString("domain_category"));
        item.setRating(rs.getDouble("rating"));
        item.setImagePath(rs.getString("image_path"));
        item.setTags(rs.getString("tags"));
        item.setDescription(rs.getString("description"));
        item.setAvailability(rs.getString("availability"));
        item.setSubCategory(rs.getString("sub_category"));
        return item;
    }

    private String buildIdList(List<ScoredResult> results) {
        if (results.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            sb.append(results.get(i).getItem().getId());
            if (i < results.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private double calculateScore(Item item, UserPreferences prefs, List<String> keywords) {
        double score = item.getRating() * 2.0; 

        // Category Matching Boost
        String itemDomain = item.getDomainCategory();
        String preferredDomain = prefs.getDomain();
        if (preferredDomain == null || preferredDomain.equalsIgnoreCase("All")) {
            preferredDomain = prefs.getDetectedDomain();
        }

        if (preferredDomain != null && preferredDomain.equalsIgnoreCase(itemDomain)) {
            score += 30.0; // Significant boost for correct category
        }

        // 1. Personalized Diet Preference Boost/Penalty
        String userDiet = prefs.getDiet();
        String itemDiet = item.getDietType();
        if (userDiet != null && !userDiet.trim().isEmpty() && !userDiet.equalsIgnoreCase("Any")) {
            if (userDiet.equalsIgnoreCase("Veg")) {
                if ("Non-Veg".equalsIgnoreCase(itemDiet)) {
                    score -= 100.0; // Heavy penalty for Veg user seeing Non-Veg items
                } else if ("Veg".equalsIgnoreCase(itemDiet)) {
                    score += 25.0;  // Boost for match
                }
            } else if (userDiet.equalsIgnoreCase("Non-Veg")) {
                if ("Non-Veg".equalsIgnoreCase(itemDiet)) {
                    score += 25.0;  // Boost for match
                }
            }
        }

        // 2. Personalized Budget Preference Boost
        String userBudget = prefs.getBudget();
        double price = item.getPrice();
        if (userBudget != null && !userBudget.trim().isEmpty()) {
            boolean fitsBudget = false;
            if (userBudget.equalsIgnoreCase("Low") && price <= 20.0) {
                fitsBudget = true;
            } else if (userBudget.equalsIgnoreCase("Medium") && price > 20.0 && price <= 100.0) {
                fitsBudget = true;
            } else if (userBudget.equalsIgnoreCase("High") && price > 100.0) {
                fitsBudget = true;
            }
            
            if (fitsBudget) {
                score += 20.0; // Boost for matching user's budget bracket
            }
        }

        if (!keywords.isEmpty()) {
            String name = (item.getName() != null ? item.getName() : "").toLowerCase();
            String tags = (item.getTags() != null ? item.getTags() : "").toLowerCase();
            String desc = (item.getDescription() != null ? item.getDescription() : "").toLowerCase();

            int matchCount = 0;
            for (String kw : keywords) {
                boolean matched = false;
                if (name.contains(kw)) {
                    score += 20.0; 
                    matched = true;
                }
                if (tags.contains(kw)) {
                    score += 10.0;
                    matched = true;
                }
                if (desc.contains(kw)) {
                    score += 5.0;
                    matched = true;
                }
                if (matched) matchCount++;
            }

            if (matchCount == keywords.size() && keywords.size() > 1) {
                score += 50.0;
            }
        }
        
        return score;
    }
}
