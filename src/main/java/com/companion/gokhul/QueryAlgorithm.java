package com.companion.gokhul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
// Removed redundant import from same package

/**
 * QueryAlgorithm handles the processing and transformation of user input.
 */
public class QueryAlgorithm {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "give", "me", "best", "choice", "the", "a", "an", "for", "with", "is", "are", "in", "on", "at", "to", "of"
    ));

    private static final Map<String, String> DOMAIN_MAP = new HashMap<>();
    static {
        DOMAIN_MAP.put("food", "Food & restaurants");
        DOMAIN_MAP.put("restaurant", "Food & restaurants");
        DOMAIN_MAP.put("eat", "Food & restaurants");
        DOMAIN_MAP.put("laptop", "Shopping & products");
        DOMAIN_MAP.put("pc", "Shopping & products");
        DOMAIN_MAP.put("computer", "Shopping & products");
        DOMAIN_MAP.put("tv", "Shopping & products");
        DOMAIN_MAP.put("television", "Shopping & products");
        DOMAIN_MAP.put("phone", "Shopping & products");
        DOMAIN_MAP.put("mobile", "Shopping & products");
        DOMAIN_MAP.put("travel", "Travel & places");
        DOMAIN_MAP.put("place", "Travel & places");
        DOMAIN_MAP.put("hotel", "Travel & places");
        DOMAIN_MAP.put("movie", "Entertainment");
        DOMAIN_MAP.put("game", "Entertainment");
        DOMAIN_MAP.put("book", "Books & Media");
    }

    /**
     * Algorithm: Pattern Matching & Semantic Extraction
     * Parses the raw query to extract price constraints and detect domain.
     */
    public static void parseQuery(UserPreferences prefs) {
        String query = prefs.getSearchQuery().toLowerCase();
        
        // 1. Price Constraint Extraction
        Pattern underPattern = Pattern.compile("(under|below|less than|within|budget|max)\\s+([0-9]+)");
        Matcher underMatcher = underPattern.matcher(query);
        if (underMatcher.find()) {
            prefs.setMaxPrice(Double.parseDouble(underMatcher.group(2)));
        }

        Pattern abovePattern = Pattern.compile("(above|over|more than|min)\\s+([0-9]+)");
        Matcher aboveMatcher = abovePattern.matcher(query);
        if (aboveMatcher.find()) {
            prefs.setMinPrice(Double.parseDouble(aboveMatcher.group(2)));
        }

        // 2. Domain Detection
        for (Map.Entry<String, String> entry : DOMAIN_MAP.entrySet()) {
            if (query.contains(entry.getKey())) {
                prefs.setDetectedDomain(entry.getValue());
                break; 
            }
        }
    }

    /**
     * Algorithm: Tokenization & Refinement
     */
    public static List<String> extractKeywords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Split and filter
        String[] words = query.toLowerCase().split("[^a-zA-Z0-9]+");
        List<String> keywords = new ArrayList<>();
        
        // Words used in price constraints should be ignored as search terms
        Set<String> constraintWords = new HashSet<>(Arrays.asList(
            "under", "below", "less", "than", "within", "budget", "max", 
            "above", "over", "more", "min"
        ));

        for (String w : words) {
            if (w.length() > 1 && !STOP_WORDS.contains(w) && !constraintWords.contains(w) && !isNumeric(w)) {
                keywords.add(w);
                
                // Basic synonym expansion
                if (w.equals("laptop") || w.equals("pc")) keywords.add("computer");
                else if (w.equals("phone")) keywords.add("mobile");
                else if (w.equals("movie")) keywords.add("cinema");
                else if (w.equals("food")) keywords.add("restaurant");
                else if (w.equals("cheap")) keywords.add("affordable");
                else if (w.equals("best")) keywords.add("top");
            }
        }
        return keywords;
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Algorithm: Dynamic SQL Construction with Parameterized Constraints
     */
    public static String buildSqlQuery(UserPreferences prefs, List<String> keywords) {
        StringBuilder sql = new StringBuilder("SELECT * FROM items WHERE 1=1 ");
        
        // 1. Explicit or Detected Domain Filter
        String domain = prefs.getDomain();
        if (domain == null || domain.equalsIgnoreCase("All")) {
            domain = prefs.getDetectedDomain();
        }

        if (domain != null && !domain.equalsIgnoreCase("All") && !domain.isEmpty()) {
            sql.append(" AND domain_category = ? ");
        }
        
        // 2. Price Filters
        if (prefs.getMinPrice() != null) {
            sql.append(" AND price >= ? ");
        }
        if (prefs.getMaxPrice() != null) {
            sql.append(" AND price <= ? ");
        }

        // 3. Diet Filter
        if (prefs.getDiet() != null && !prefs.getDiet().equalsIgnoreCase("Any") && !prefs.getDiet().isEmpty()) {
            sql.append(" AND (diet_type = ? OR diet_type IS NULL OR diet_type = '') ");
        }

        // 4. Keyword Search (Upgraded to FULLTEXT MATCH AGAINST)
        if (!keywords.isEmpty()) {
            sql.append(" AND MATCH(name, tags, description) AGAINST (? IN BOOLEAN MODE) ");
        }

        sql.append(" ORDER BY rating DESC LIMIT 1000"); 
        return sql.toString();
    }
}
