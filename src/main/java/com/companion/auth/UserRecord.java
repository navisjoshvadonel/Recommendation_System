package com.companion.auth;

public class UserRecord {
    private int id;
    private String username;
    private String email;
    private String dietPref;
    private String budgetPref;
    private String role; // "user" or "admin"

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDietPref() { return dietPref; }
    public void setDietPref(String dietPref) { this.dietPref = dietPref; }

    public String getBudgetPref() { return budgetPref; }
    public void setBudgetPref(String budgetPref) { this.budgetPref = budgetPref; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
