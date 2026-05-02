package com.companion.navis;

import com.companion.admin.AdminPanel;
import com.companion.auth.Session;
import com.companion.auth.UserDAO;
import com.companion.auth.UserRecord;
import com.companion.gokhul.UserPreferences;
import com.companion.karan.BackgroundThreadManager;
// Other imports from same package (RecommendationEngine, ScoredResult, GlassCard) are handled automatically

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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

    public MainApplication() {
        setTitle("Recommendation Companion - Professional Edition");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        
        contentLayout = new CardLayout();
        mainContent = new JPanel(contentLayout);
        
        // Add Login/Register outside the main sidebar layout
        JPanel authPanel = new JPanel(new CardLayout());
        authPanel.add(createLoginPanel(), "LOGIN");
        authPanel.add(createRegisterPanel(), "REGISTER");
        
        add(authPanel, BorderLayout.CENTER);
        
        // Success listener to switch to Dashboard
        this.getContentPane().revalidate();
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
        panel.add(Box.createRigidArea(new Dimension(0, 40)));

        panel.add(createSidebarButton("Discover", "SEARCH"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        UserRecord current = Session.getInstance().getCurrentUser();
        if (current != null && current.isAdmin()) {
            panel.add(createSidebarButton("Admin Panel", "ADMIN"));
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        panel.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Logout");
        styleSidebarButton(btnLogout);
        btnLogout.addActionListener(e -> {
            Session.getInstance().logout();
            System.exit(0); // Simple logout for now or switch back to Login
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
        card.setPreferredSize(new Dimension(400, 350));
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
        gbc.insets = new Insets(25, 12, 12, 12);
        JButton btnLogin = new JButton("Login");
        btnLogin.setPreferredSize(new Dimension(200, 45));
        btnLogin.setBackground(PRIMARY);
        btnLogin.setForeground(Color.WHITE); // ENSURE WHITE TEXT IF PRIMARY IS DARK
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
        
        panel.add(card);
        return panel;
    }

    private JPanel createRegisterPanel() {
        // Implementation similar to login, kept simple
        return new JPanel(); 
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        
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
        
        PlaceholderTextField searchField = new PlaceholderTextField("Enter your query here...");
        searchField.setFont(new Font("Inter", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(400, 45));
        
        JComboBox<String> domainCombo = new JComboBox<>(new String[]{
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
            resultsPanel.removeAll();
            resultsPanel.add(new JLabel("Searching for related items..."));
            resultsPanel.revalidate();
            resultsPanel.repaint();

            UserPreferences prefs = new UserPreferences();
            prefs.setSearchQuery(searchField.getText().equals("Enter your query here...") ? "" : searchField.getText());
            prefs.setDomain((String) domainCombo.getSelectedItem());
            
            BackgroundThreadManager.getInstance().execute(() -> {
                List<ScoredResult> recs = engine.getRecommendations(prefs);
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
                });
            });
        });
        return panel;
    }
    
    private JPanel createItemCard(ScoredResult result) {
        GlassCard card = new GlassCard(15);
        card.setLayout(new BorderLayout(20, 20));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setMaximumSize(new Dimension(1000, 160));
        card.setBackgroundColor(WHITE);
        
        // No image preview requested

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
        JLabel badge = new JLabel(String.format("%.0f%% Match", Math.min(100, result.getScore() * 2)));
        badge.setFont(new Font("Inter", Font.BOLD, 12));
        badge.setForeground(PRIMARY);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        badgePanel.add(badge, BorderLayout.NORTH);
        card.add(badgePanel, BorderLayout.EAST);

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
