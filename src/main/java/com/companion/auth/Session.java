package com.companion.auth;

public class Session {
    private static Session instance;
    private UserRecord currentUser;

    private Session() {
    }

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void login(UserRecord user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public UserRecord getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
