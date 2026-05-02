package com.companion.admin;

import com.companion.karan.BackgroundThreadManager;
// AnalyticsDAO is in same package

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {
    private AnalyticsDAO analyticsDAO;
    private JLabel totalUsersLabel;
    private JLabel totalItemsLabel;

    public AdminPanel() {
        this.analyticsDAO = new AnalyticsDAO();
        setLayout(new BorderLayout());
        setBackground(Color.decode("#ECFEFF")); // BG_LIGHT

        // Title
        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(Color.decode("#164E63")); // TEXT_DARK
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Center Content (Statistics)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        totalUsersLabel = new JLabel("Total Users: 0");
        totalUsersLabel.setFont(new Font("Inter", Font.BOLD, 18));
        totalUsersLabel.setForeground(Color.decode("#0E7490"));

        totalItemsLabel = new JLabel("Total Items: 0");
        totalItemsLabel.setFont(new Font("Inter", Font.BOLD, 18));
        totalItemsLabel.setForeground(Color.decode("#0E7490"));

        gbc.gridy = 0;
        centerPanel.add(totalUsersLabel, gbc);
        gbc.gridy = 1;
        centerPanel.add(totalItemsLabel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Action Panel (Simple Refresh)
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Statistics");
        refreshBtn.setBackground(Color.decode("#0891B2")); 
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Inter", Font.BOLD, 14));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        refreshBtn.addActionListener(e -> refreshStats());
        
        JButton addItemBtn = new JButton("Add New Item +");
        addItemBtn.setBackground(Color.decode("#059669")); // Green
        addItemBtn.setForeground(Color.WHITE);
        addItemBtn.setFont(new Font("Inter", Font.BOLD, 14));
        addItemBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addItemBtn.addActionListener(e -> showAddItemDialog());

        actionPanel.add(refreshBtn);
        actionPanel.add(addItemBtn);
        add(actionPanel, BorderLayout.SOUTH);

        refreshStats();
    }

    private void showAddItemDialog() {
        JDialog dialog = new JDialog((Frame)null, "Create New Recommendation Item", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField priceField = new JTextField("0.0");
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
            "Food & restaurants", "Shopping & products", "Entertainment", 
            "Travel & places", "Education & courses", "Real Estate", 
            "Vehicles", "Health & Wellness", "Art & Collectibles", 
            "Professional Services", "Books & Media", "Software & Digital"
        });
        JTextField ratingField = new JTextField("4.5");
        JTextField tagsField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JTextField subCatField = new JTextField();
        JComboBox<String> dietCombo = new JComboBox<>(new String[]{"None", "Veg", "Non-Veg"});

        int row = 0;
        addFormField(dialog, "Name:", nameField, row++, gbc);
        addFormField(dialog, "Price:", priceField, row++, gbc);
        addFormField(dialog, "Category:", categoryCombo, row++, gbc);
        addFormField(dialog, "Rating:", ratingField, row++, gbc);
        addFormField(dialog, "Tags (comma separated):", tagsField, row++, gbc);
        addFormField(dialog, "Description:", new JScrollPane(descArea), row++, gbc);
        addFormField(dialog, "Sub-Category:", subCatField, row++, gbc);
        addFormField(dialog, "Diet Type:", dietCombo, row++, gbc);

        JButton saveBtn = new JButton("Save to Database");
        saveBtn.setBackground(Color.decode("#0891B2"));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                String diet = dietCombo.getSelectedItem().equals("None") ? null : (String)dietCombo.getSelectedItem();
                boolean success = analyticsDAO.addItem(
                    nameField.getText(),
                    Double.parseDouble(priceField.getText()),
                    (String)categoryCombo.getSelectedItem(),
                    Double.parseDouble(ratingField.getText()),
                    tagsField.getText(),
                    descArea.getText(),
                    subCatField.getText(),
                    diet
                );

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Item added successfully!");
                    dialog.dispose();
                    refreshStats();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error adding item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input data: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        dialog.add(saveBtn, gbc);

        dialog.setVisible(true);
    }

    private void addFormField(JDialog dialog, String label, JComponent field, int row, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.3;
        dialog.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        dialog.add(field, gbc);
    }

    private void refreshStats() {
        BackgroundThreadManager.getInstance().execute(() -> {
            int users = analyticsDAO.getTotalUsers();
            int items = analyticsDAO.getTotalItems();
            SwingUtilities.invokeLater(() -> {
                totalUsersLabel.setText("Total Users: " + users);
                totalItemsLabel.setText("Total Items: " + items);
            });
        });
    }
}
