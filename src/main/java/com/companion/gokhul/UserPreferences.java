package com.companion.gokhul;

public class UserPreferences {
    private String domain;
    private String budget;
    private String diet;
    private String searchQuery;
    private String sortBy;
    private Double minPrice;
    private Double maxPrice;
    private String detectedDomain;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }

    public String getDiet() { return diet; }
    public void setDiet(String diet) { this.diet = diet; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public String getDetectedDomain() { return detectedDomain; }
    public void setDetectedDomain(String detectedDomain) { this.detectedDomain = detectedDomain; }
}
