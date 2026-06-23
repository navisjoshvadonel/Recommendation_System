package com.companion.navis;

import com.companion.admin.AdminPanel;
import com.companion.admin.AnalyticsDAO;
import com.companion.auth.Session;
import com.companion.auth.UserDAO;
import com.companion.auth.UserRecord;
import com.companion.gokhul.UserPreferences;
import com.companion.karan.BackgroundThreadManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainApplication extends JFrame {
    
    // Theme Colors
    public static final Color PRIMARY      = Color.decode("#6366F1"); // Indigo 500
    public static final Color SIDEBAR_BG   = Color.decode("#0F172A"); // Slate 900
    public static final Color BG_LIGHT     = Color.decode("#F1F5F9"); // Slate 100
    public static final Color TEXT_DARK    = Color.decode("#1E293B"); // Slate 800 (Charcoal)
    public static final Color TEXT_DESC    = Color.decode("#475569"); // Slate 600
    public static final Color BORDER       = Color.decode("#CBD5E1"); // Slate 300
    public static final Color WHITE        = Color.WHITE;

    private UserDAO userDAO = new UserDAO();
    private RecommendationEngine engine = new RecommendationEngine();

    private JPanel mainContent;
    private CardLayout contentLayout;
    private JPanel sidebar;
    
    // Auth Panel Fields
    private JPanel authPanel;
    private CardLayout authLayout;
    
    // Recent Searches Panel Reference
    private JPanel recentSearchesPanel;
    private PlaceholderTextField searchField;
    private JComboBox<String> domainCombo;

    public MainApplication() {
        setTitle("Recommendation Companion - Professional Edition");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        
        contentLayout = new CardLayout();
        mainContent = new JPanel(contentLayout);
        
        showAuthScreen();
    }

    private void showAuthScreen() {
        getContentPane().removeAll();
        
        authLayout = new CardLayout();
        authPanel = new JPanel(authLayout);
        authPanel.add(createLoginPanel(), "LOGIN");
        authPanel.add(createRegisterPanel(), "REGISTER");
        
        add(authPanel, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private void switchToDashboard() {
        getContentPane().removeAll();
        
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        mainContent.add(createSearchPanel(), "SEARCH");
        UserRecord current = Session.getInstance().getCurrentUser();
        if (current != null && current.isAdmin()) {
            mainContent.add(new AdminPanel(), "ADMIN");
        }
        
        add(mainContent, BorderLayout.CENTER);
        
        contentLayout.show(mainContent, "SEARCH");
        revalidate();
        repaint();
        
        // Load recent searches on transition
        refreshRecentSearches();
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_BG);
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel brand = new JLabel("Ethereal Prec.");
        brand.setFont(new Font("Inter", Font.BOLD, 22));
        brand.setForeground(WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(brand);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        panel.add(createSidebarButton("Discover", "SEARCH"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        UserRecord current = Session.getInstance().getCurrentUser();
        if (current != null && current.isAdmin()) {
            panel.add(createSidebarButton("Admin Panel", "ADMIN"));
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Recent Searches Container in Sidebar
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel recentTitle = new JLabel("Recent Queries");
        recentTitle.setFont(new Font("Inter", Font.BOLD, 12));
        recentTitle.setForeground(Color.decode("#64748B"));
        recentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(recentTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        recentSearchesPanel = new JPanel();
        recentSearchesPanel.setLayout(new BoxLayout(recentSearchesPanel, BoxLayout.Y_AXIS));
        recentSearchesPanel.setOpaque(false);
        recentSearchesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(recentSearchesPanel);

        panel.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Logout");
        styleSidebarButton(btnLogout);
        btnLogout.addActionListener(e -> {
            Session.getInstance().logout();
            showAuthScreen();
        });
        panel.add(btnLogout);

        return panel;
    }

    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text);
        styleSidebarButton(btn);
        btn.addActionListener(e -> contentLayout.show(mainContent, cardName));
        return btn;
    }

    private void styleSidebarButton(JButton btn) {
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 45));
        btn.setFont(new Font("Inter", Font.PLAIN, 16));
        btn.setForeground(Color.decode("#94A3B8")); // Slate 400
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.decode("#94A3B8"));
            }
        });
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_LIGHT);
        
        GlassCard card = new GlassCard(20);
        card.setPreferredSize(new Dimension(400, 380));
        card.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Welcome Back", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(PRIMARY);
        card.add(title, gbc);
        
        // Username
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Inter", Font.BOLD, 14));
        userLabel.setForeground(TEXT_DARK);
        card.add(userLabel, gbc);
        
        gbc.gridx = 1;
        JTextField txtUser = new JTextField("admin", 15);
        txtUser.setFont(new Font("Inter", Font.PLAIN, 14));
        txtUser.setForeground(TEXT_DARK);
        card.add(txtUser, gbc);
        
        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Inter", Font.BOLD, 14));
        passLabel.setForeground(TEXT_DARK);
        card.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField txtPass = new JPasswordField("admin123", 15);
        txtPass.setFont(new Font("Inter", Font.PLAIN, 14));
        txtPass.setForeground(TEXT_DARK);
        card.add(txtPass, gbc);
        
        // Login Button
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 12, 5, 12);
        JButton btnLogin = new JButton("Login");
        btnLogin.setPreferredSize(new Dimension(200, 45));
        btnLogin.setBackground(PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Inter", Font.BOLD, 16));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        
        btnLogin.addActionListener(e -> {
            String uname = txtUser.getText().trim();
            String pass  = new String(txtPass.getPassword());
            UserRecord user = userDAO.login(uname, pass);
            if (user != null) {
                Session.getInstance().login(user);
                switchToDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(btnLogin, gbc);

        // Go to Register Button
        gbc.gridy = 4; gbc.insets = new Insets(5, 12, 12, 12);
        JButton btnGoRegister = new JButton("Don't have an account? Register");
        btnGoRegister.setFont(new Font("Inter", Font.PLAIN, 12));
        btnGoRegister.setForeground(PRIMARY);
        btnGoRegister.setContentAreaFilled(false);
        btnGoRegister.setBorderPainted(false);
        btnGoRegister.setFocusPainted(false);
        btnGoRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoRegister.addActionListener(e -> authLayout.show(authPanel, "REGISTER"));
        card.add(btnGoRegister, gbc);
        
        panel.add(card);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_LIGHT);
        
        GlassCard card = new GlassCard(20);
        card.setPreferredSize(new Dimension(420, 480));
        card.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 26));
        title.setForeground(PRIMARY);
        card.add(title, gbc);
        
        // Username
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Inter", Font.BOLD, 13));
        userLabel.setForeground(TEXT_DARK);
        card.add(userLabel, gbc);
        
        gbc.gridx = 1;
        JTextField txtUser = new JTextField(15);
        txtUser.setFont(new Font("Inter", Font.PLAIN, 13));
        card.add(txtUser, gbc);
        
        // Email
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Inter", Font.BOLD, 13));
        emailLabel.setForeground(TEXT_DARK);
        card.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        JTextField txtEmail = new JTextField(15);
        txtEmail.setFont(new Font("Inter", Font.PLAIN, 13));
        card.add(txtEmail, gbc);
        
        // Password
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Inter", Font.BOLD, 13));
        passLabel.setForeground(TEXT_DARK);
        card.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField txtPass = new JPasswordField(15);
        txtPass.setFont(new Font("Inter", Font.PLAIN, 13));
        card.add(txtPass, gbc);

        // Diet Preference
        gbc.gridy = 4; gbc.gridx = 0;
        JLabel dietLabel = new JLabel("Diet Pref:");
        dietLabel.setFont(new Font("Inter", Font.BOLD, 13));
        dietLabel.setForeground(TEXT_DARK);
        card.add(dietLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> dietCombo = new JComboBox<>(new String[]{"Any", "Veg", "Non-Veg"});
        dietCombo.setFont(new Font("Inter", Font.PLAIN, 13));
        card.add(dietCombo, gbc);

        // Budget Preference
        gbc.gridy = 5; gbc.gridx = 0;
        JLabel budgetLabel = new JLabel("Budget Pref:");
        budgetLabel.setFont(new Font("Inter", Font.BOLD, 13));
        budgetLabel.setForeground(TEXT_DARK);
        card.add(budgetLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> budgetCombo = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        budgetCombo.setFont(new Font("Inter", Font.PLAIN, 13));
        card.add(budgetCombo, gbc);
        
        // Register Button
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 12, 5, 12);
        JButton btnRegister = new JButton("Register");
        btnRegister.setPreferredSize(new Dimension(200, 45));
        btnRegister.setBackground(PRIMARY);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Inter", Font.BOLD, 15));
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setOpaque(true);
        
        btnRegister.addActionListener(e -> {
            String uname = txtUser.getText().trim();
            String email = txtEmail.getText().trim();
            String pass  = new String(txtPass.getPassword());
            String diet  = (String) dietCombo.getSelectedItem();
            String budget = (String) budgetCombo.getSelectedItem();
            
            if (uname.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            UserDAO.RegisterResult res = userDAO.register(uname, email, pass, budget, diet, "user");
            if (res == UserDAO.RegisterResult.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                authLayout.show(authPanel, "LOGIN");
            } else if (res == UserDAO.RegisterResult.DUPLICATE_USERNAME) {
                JOptionPane.showMessageDialog(this, "Username is already taken.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (res == UserDAO.RegisterResult.DUPLICATE_EMAIL) {
                JOptionPane.showMessageDialog(this, "Email is already registered.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(btnRegister, gbc);
        
        // Back to Login Link
        gbc.gridy = 7; gbc.insets = new Insets(5, 12, 12, 12);
        JButton btnBack = new JButton("Back to Login");
        btnBack.setFont(new Font("Inter", Font.PLAIN, 12));
        btnBack.setForeground(PRIMARY);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> authLayout.show(authPanel, "LOGIN"));
        card.add(btnBack, gbc);
        
        panel.add(card);
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel title = new JLabel("Recommendation Companion");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        header.add(title, BorderLayout.WEST);

        panel.add(header, BorderLayout.NORTH);

        // Controls
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        
        searchField = new PlaceholderTextField("Enter your query here...");
        searchField.setFont(new Font("Inter", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(400, 45));
        
        domainCombo = new JComboBox<>(new String[]{
            "All", "Food & restaurants", "Shopping & products", "Entertainment", 
            "Travel & places", "Education & courses", "Real Estate", 
            "Vehicles", "Health & Wellness", "Art & Collectibles", 
            "Professional Services", "Books & Media", "Software & Digital"
        });
        domainCombo.setPreferredSize(new Dimension(180, 45));
        
        JButton btnSearch = new JButton("Search Recommendations");
        btnSearch.setPreferredSize(new Dimension(220, 45));
        btnSearch.setBackground(PRIMARY);
        btnSearch.setForeground(WHITE);
        btnSearch.setFont(new Font("Inter", Font.BOLD, 14));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setOpaque(true);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0; gbc.weightx = 1.0;
        controls.add(searchField, gbc);
        gbc.gridx = 1; gbc.weightx = 0;
        controls.add(domainCombo, gbc);
        gbc.gridx = 2;
        controls.add(btnSearch, gbc);

        // Results Container
        JPanel resultsScrollContainer = new JPanel(new BorderLayout());
        resultsScrollContainer.setOpaque(false);
        resultsScrollContainer.add(controls, BorderLayout.NORTH);

        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BG_LIGHT);
        resultsPanel.setBorder(new EmptyBorder(0, 30, 20, 30));
        
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        resultsScrollContainer.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(resultsScrollContainer, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            String rawQuery = searchField.getText();
            String queryVal = rawQuery.equals("Enter your query here...") ? "" : rawQuery;
            
            resultsPanel.removeAll();
            resultsPanel.add(new JLabel("Searching for related items..."));
            resultsPanel.revalidate();
            resultsPanel.repaint();

            UserPreferences prefs = new UserPreferences();
            prefs.setSearchQuery(queryVal);
            prefs.setDomain((String) domainCombo.getSelectedItem());
            
            UserRecord currentUser = Session.getInstance().getCurrentUser();
            if (currentUser != null) {
                prefs.setDiet(currentUser.getDietPref());
                prefs.setBudget(currentUser.getBudgetPref());
            }
            
            BackgroundThreadManager.getInstance().execute(() -> {
                List<ScoredResult> recs = engine.getRecommendations(prefs);
                
                // Log Search History in Database
                if (currentUser != null) {
                    new AnalyticsDAO().logSearch(currentUser.getId(), queryVal, (String) domainCombo.getSelectedItem(), recs.size());
                }
                
                SwingUtilities.invokeLater(() -> {
                    resultsPanel.removeAll();
                    if (recs.isEmpty()) {
                        resultsPanel.add(new JLabel("No matching results found."));
                    } else {
                        for (ScoredResult sr : recs) {
                            resultsPanel.add(createItemCard(sr));
                            resultsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                        }
                    }
                    resultsPanel.revalidate();
                    resultsPanel.repaint();
                    
                    // Refresh recent queries list in sidebar
                    refreshRecentSearches();
                });
            });
        });
        return panel;
    }
    
    private void refreshRecentSearches() {
        if (recentSearchesPanel == null) return;
        recentSearchesPanel.removeAll();
        UserRecord current = Session.getInstance().getCurrentUser();
        if (current != null) {
            BackgroundThreadManager.getInstance().execute(() -> {
                List<String> recentList = new AnalyticsDAO().getRecentSearches(current.getId());
                SwingUtilities.invokeLater(() -> {
                    if (recentList.isEmpty()) {
                        JLabel empty = new JLabel("No recent queries");
                        empty.setFont(new Font("Inter", Font.ITALIC, 13));
                        empty.setForeground(Color.decode("#475569"));
                        recentSearchesPanel.add(empty);
                    } else {
                        for (String q : recentList) {
                            JButton btn = new JButton(q);
                            btn.setFont(new Font("Inter", Font.PLAIN, 13));
                            btn.setForeground(Color.decode("#94A3B8"));
                            btn.setBackground(SIDEBAR_BG);
                            btn.setBorderPainted(false);
                            btn.setFocusPainted(false);
                            btn.setContentAreaFilled(false);
                            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
                            btn.addActionListener(e -> {
                                if (searchField != null) {
                                    searchField.setText(q);
                                    searchField.setForeground(TEXT_DARK);
                                    // Trigger search
                                    for (java.awt.event.ActionListener al : searchField.getActionListeners()) {
                                        al.actionPerformed(e);
                                    }
                                }
                            });
                            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                                public void mouseEntered(java.awt.event.MouseEvent evt) {
                                    btn.setForeground(WHITE);
                                }
                                public void mouseExited(java.awt.event.MouseEvent evt) {
                                    btn.setForeground(Color.decode("#94A3B8"));
                                }
                            });
                            recentSearchesPanel.add(btn);
                            recentSearchesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                        }
                    }
                    recentSearchesPanel.revalidate();
                    recentSearchesPanel.repaint();
                });
            });
        }
    }

    private ImageIcon getIcon(String path) {
        java.net.URL imgURL = getClass().getResource("/" + path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            // Fallback to relative filesystem loading
            File file = new File("src/main/resources/" + path);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
            file = new File("resources/" + path);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
        }
        return null;
    }
    
    private JPanel createItemCard(ScoredResult result) {
        GlassCard card = new GlassCard(15);
        card.setLayout(new BorderLayout(20, 20));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setMaximumSize(new Dimension(1000, 160));
        card.setBackgroundColor(WHITE);

        // Icon Panel on the Left
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        
        String category = result.getItem().getDomainCategory();
        String name = result.getItem().getName().toLowerCase();
        String tags = (result.getItem().getTags() != null ? result.getItem().getTags() : "").toLowerCase();
        
        String iconPath = null;
        if ("Food & restaurants".equalsIgnoreCase(category) || tags.contains("food") || tags.contains("eat")) {
            if (tags.contains("burger") || tags.contains("pav") || name.contains("burger") || name.contains("pav")) {
                iconPath = "assets/burger.png";
            } else {
                iconPath = "assets/pizza.png";
            }
        } else if ("Shopping & products".equalsIgnoreCase(category) || tags.contains("shopping")) {
            if (tags.contains("laptop") || name.contains("laptop") || name.contains("computer")) {
                iconPath = "assets/laptop.png";
            } else if (tags.contains("headphones") || name.contains("headphones") || tags.contains("audio") || tags.contains("music")) {
                iconPath = "assets/headphones.png";
            } else {
                iconPath = "assets/laptop.png";
            }
        } else if ("Travel & places".equalsIgnoreCase(category) || tags.contains("travel") || tags.contains("tour") || tags.contains("trip")) {
            iconPath = "assets/travel.png";
        } else if ("Education & courses".equalsIgnoreCase(category) || tags.contains("education") || tags.contains("course") || tags.contains("masterclass")) {
            iconPath = "assets/education.png";
        } else if ("Entertainment".equalsIgnoreCase(category) || tags.contains("movie") || tags.contains("game") || tags.contains("entertainment") || tags.contains("cinema")) {
            iconPath = "assets/entertainment.png";
        }

        ImageIcon icon = null;
        if (iconPath != null) {
            icon = getIcon(iconPath);
        }

        if (icon != null) {
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImg));
            leftPanel.add(iconLabel);
        } else {
            // Elegant text circle fallback
            final String firstChar = (category != null && !category.isEmpty()) ? category.substring(0, 1).toUpperCase() : "?";
            JPanel badgePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    GradientPaint gp = new GradientPaint(0, 0, PRIMARY, 0, getHeight(), PRIMARY.darker());
                    g2.setPaint(gp);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Inter", Font.BOLD, 22));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(firstChar)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(firstChar, x, y);
                    g2.dispose();
                }
            };
            badgePanel.setPreferredSize(new Dimension(50, 50));
            badgePanel.setOpaque(false);
            leftPanel.add(badgePanel);
        }
        card.add(leftPanel, BorderLayout.WEST);

        // Meta Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(result.getItem().getName());
        nameLabel.setFont(new Font("Inter", Font.BOLD, 20));
        nameLabel.setForeground(TEXT_DARK);
        
        JLabel descLabel = new JLabel("<html><body style='width: 400px'>" + result.getItem().getDescription() + "</body></html>");
        descLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        descLabel.setForeground(TEXT_DESC);
        
        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        tagsPanel.setOpaque(false);
        JLabel priceLabel = new JLabel("$" + result.getItem().getPrice() + "  ");
        priceLabel.setFont(new Font("Inter", Font.BOLD, 16));
        priceLabel.setForeground(PRIMARY);
        
        JLabel scoreLabel = new JLabel("Rating: " + result.getItem().getRating() + " ★");
        scoreLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        scoreLabel.setForeground(Color.decode("#CA8A04")); // Yellow 600
        
        tagsPanel.add(priceLabel);
        tagsPanel.add(scoreLabel);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(tagsPanel);

        card.add(infoPanel, BorderLayout.CENTER);
        
        // Match Score Badge
        JPanel badgePanel = new JPanel(new BorderLayout());
        badgePanel.setOpaque(false);
        
        double displayScore = result.getScore();
        // Shift score from raw range to logical percentage
        if (displayScore < 0.0) displayScore = 0.0;
        JLabel badge = new JLabel(String.format("%.0f%% Match", Math.min(100.0, displayScore)));
        badge.setFont(new Font("Inter", Font.BOLD, 12));
        badge.setForeground(PRIMARY);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        badgePanel.add(badge, BorderLayout.NORTH);
        
        // Like Button
        JButton likeBtn = new JButton("❤ Like");
        likeBtn.setFont(new Font("Inter", Font.BOLD, 12));
        likeBtn.setForeground(Color.WHITE);
        likeBtn.setBackground(Color.decode("#EC4899")); // Pink
        likeBtn.setFocusPainted(false);
        likeBtn.setBorderPainted(false);
        likeBtn.setOpaque(true);
        likeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        likeBtn.addActionListener(e -> {
            UserRecord current = Session.getInstance().getCurrentUser();
            if (current != null) {
                BackgroundThreadManager.getInstance().execute(() -> {
                    boolean success = new com.companion.auth.InteractionDAO().logInteraction(current.getId(), result.getItem().getId(), "like");
                    if (success) {
                        SwingUtilities.invokeLater(() -> {
                            likeBtn.setText("Liked");
                            likeBtn.setBackground(Color.decode("#9CA3AF")); // Gray
                            likeBtn.setEnabled(false);
                        });
                    }
                });
            } else {
                JOptionPane.showMessageDialog(card, "Please login to like items.");
            }
        });
        
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(badgePanel, BorderLayout.NORTH);
        rightPanel.add(likeBtn, BorderLayout.SOUTH);
        card.add(rightPanel, BorderLayout.EAST);

        // Hover Effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackgroundColor(Color.decode("#F8FAFC"));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackgroundColor(WHITE);
            }
        });

        return card;
    }

    // Helper for placeholder text
    class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setForeground(Color.GRAY);
            setText(placeholder);
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(TEXT_DARK);
                    }
                }
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
            // Support pressing Enter key to run search
            addActionListener(e -> {
                // Trigger action on the search button dynamically
            });
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new MainApplication().setVisible(true);
        });
    }
}
