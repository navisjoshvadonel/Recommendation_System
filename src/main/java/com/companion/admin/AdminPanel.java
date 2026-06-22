package com.companion.admin;

import com.companion.karan.BackgroundThreadManager;
import com.companion.navis.CsvItem;
import com.companion.navis.GlassCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPanel extends JPanel {
    private AnalyticsDAO analyticsDAO;
    private JLabel totalUsersValLabel;
    private JLabel totalItemsValLabel;
    private JLabel totalSearchesValLabel;

    public AdminPanel() {
        this.analyticsDAO = new AnalyticsDAO();
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F1F5F9")); // Slate 100

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#CBD5E1")),
            new EmptyBorder(20, 30, 20, 30)
        ));

        JLabel title = new JLabel("Admin Control Center");
        title.setFont(new Font("Inter", Font.BOLD, 24));
        title.setForeground(Color.decode("#1E293B")); // Slate 800
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Stats Cards Panel
        JPanel statsContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        statsContainer.setOpaque(false);
        statsContainer.setBorder(new EmptyBorder(30, 30, 20, 30));

        GlassCard usersCard = createStatsCard("Registered Users", totalUsersValLabel = new JLabel("0"), Color.decode("#6366F1"));
        GlassCard itemsCard = createStatsCard("Total Recommendations", totalItemsValLabel = new JLabel("0"), Color.decode("#059669"));
        GlassCard searchesCard = createStatsCard("Analytics Searches", totalSearchesValLabel = new JLabel("0"), Color.decode("#D97706"));

        statsContainer.add(usersCard);
        statsContainer.add(itemsCard);
        statsContainer.add(searchesCard);

        add(statsContainer, BorderLayout.CENTER);

        // Footer Actions Panel
        JPanel actionsContainer = new JPanel(new BorderLayout());
        actionsContainer.setOpaque(false);
        actionsContainer.setBorder(new EmptyBorder(10, 30, 30, 30));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh Analytics");
        styleActionButton(refreshBtn, Color.decode("#475569"), Color.WHITE);
        refreshBtn.addActionListener(e -> refreshStats());
        leftActions.add(refreshBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton bulkImportBtn = new JButton("Bulk Import CSV");
        styleActionButton(bulkImportBtn, Color.decode("#2563EB"), Color.WHITE); // Royal Blue
        bulkImportBtn.addActionListener(e -> triggerBulkImport());
        rightActions.add(bulkImportBtn);

        JButton addItemBtn = new JButton("Add Single Item +");
        styleActionButton(addItemBtn, Color.decode("#059669"), Color.WHITE);
        addItemBtn.addActionListener(e -> showAddItemDialog());
        rightActions.add(addItemBtn);

        actionsContainer.add(leftActions, BorderLayout.WEST);
        actionsContainer.add(rightActions, BorderLayout.EAST);
        add(actionsContainer, BorderLayout.SOUTH);

        refreshStats();
    }

    private GlassCard createStatsCard(String title, JLabel valLabel, Color color) {
        GlassCard card = new GlassCard(16);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setBackgroundColor(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        titleLabel.setForeground(Color.decode("#64748B")); // Slate 500

        valLabel.setFont(new Font("Inter", Font.BOLD, 36));
        valLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);

        // Soft visual hover feedback
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackgroundColor(Color.decode("#F8FAFC"));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackgroundColor(Color.WHITE);
            }
        });

        return card;
    }

    private void styleActionButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 45));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    private void refreshStats() {
        BackgroundThreadManager.getInstance().execute(() -> {
            int users = analyticsDAO.getTotalUsers();
            int items = analyticsDAO.getTotalItems();
            int searches = analyticsDAO.getTotalSearches();
            SwingUtilities.invokeLater(() -> {
                totalUsersValLabel.setText(String.valueOf(users));
                totalItemsValLabel.setText(String.valueOf(items));
                totalSearchesValLabel.setText(String.valueOf(searches));
            });
        });
    }

    private void triggerBulkImport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Items CSV File to Import");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            
            // Show custom loading dialog
            JDialog progressDialog = new JDialog((Frame)null, "Importing CSV...", true);
            progressDialog.setSize(300, 120);
            progressDialog.setLayout(new GridBagLayout());
            progressDialog.setLocationRelativeTo(this);
            
            JLabel statusLabel = new JLabel("Parsing and validating rows...");
            statusLabel.setFont(new Font("Inter", Font.PLAIN, 14));
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.gridx = 0; gbc.gridy = 0;
            progressDialog.add(statusLabel, gbc);
            gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            progressDialog.add(progressBar, gbc);

            BackgroundThreadManager.getInstance().execute(() -> {
                List<CsvItem> itemsToImport = new ArrayList<>();
                int skippedRows = 0;
                
                try (BufferedReader br = new BufferedReader(new FileReader(fileToImport))) {
                    String headerLine = br.readLine();
                    if (headerLine == null) {
                        throw new Exception("Empty CSV file.");
                    }

                    List<String> headers = parseCsvLine(headerLine);
                    Map<String, Integer> colMap = new HashMap<>();
                    for (int i = 0; i < headers.size(); i++) {
                        colMap.put(headers.get(i).toLowerCase().trim(), i);
                    }

                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        List<String> row = parseCsvLine(line);
                        
                        try {
                            CsvItem item = new CsvItem();
                            item.setName(getValueByHeader(row, colMap, "name", ""));
                            item.setPrice(Double.parseDouble(getValueByHeader(row, colMap, "price", "0.0")));
                            item.setDomainCategory(getValueByHeader(row, colMap, "domain_category", "Shopping & products"));
                            item.setRating(Double.parseDouble(getValueByHeader(row, colMap, "rating", "4.0")));
                            item.setTags(getValueByHeader(row, colMap, "tags", ""));
                            item.setDescription(getValueByHeader(row, colMap, "description", ""));
                            item.setSubCategory(getValueByHeader(row, colMap, "sub_category", ""));
                            item.setDietType(getValueByHeader(row, colMap, "diet_type", null));
                            
                            if (item.isValid()) {
                                itemsToImport.add(item);
                            } else {
                                skippedRows++;
                            }
                        } catch (Exception ex) {
                            skippedRows++;
                        }
                    }
                    
                    boolean success = false;
                    if (!itemsToImport.isEmpty()) {
                        success = analyticsDAO.addItemsBatch(itemsToImport);
                    }
                    
                    final boolean finalSuccess = success;
                    final int importedCount = itemsToImport.size();
                    final int finalSkipped = skippedRows;
                    
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        if (finalSuccess || importedCount == 0) {
                            JOptionPane.showMessageDialog(this, 
                                "Import Complete!\nSuccessfully imported: " + importedCount + " items.\nSkipped rows: " + finalSkipped, 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "Failed to insert items into database. Please check connection and logs.", 
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        refreshStats();
                    });
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(this, "Error parsing CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            });
            
            progressDialog.setVisible(true);
        }
    }

    private String getValueByHeader(List<String> row, Map<String, Integer> colMap, String columnName, String defaultValue) {
        Integer index = colMap.get(columnName);
        if (index != null && index < row.size()) {
            String val = row.get(index);
            return (val == null || val.isEmpty()) ? defaultValue : val;
        }
        return defaultValue;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString().trim());
        return values;
    }

    private void showAddItemDialog() {
        JDialog dialog = new JDialog((Frame)null, "Create New Recommendation Item", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
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
        addFormField(dialog, "Price ($):", priceField, row++, gbc);
        addFormField(dialog, "Category:", categoryCombo, row++, gbc);
        addFormField(dialog, "Rating (1-5):", ratingField, row++, gbc);
        addFormField(dialog, "Tags (comma list):", tagsField, row++, gbc);
        addFormField(dialog, "Description:", new JScrollPane(descArea), row++, gbc);
        addFormField(dialog, "Sub-Category:", subCatField, row++, gbc);
        addFormField(dialog, "Diet Type:", dietCombo, row++, gbc);

        JButton saveBtn = new JButton("Save to Database");
        saveBtn.setBackground(Color.decode("#059669"));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Inter", Font.BOLD, 14));
        saveBtn.setFocusPainted(false);
        saveBtn.setOpaque(true);
        saveBtn.addActionListener(e -> {
            try {
                String diet = dietCombo.getSelectedItem().equals("None") ? null : (String)dietCombo.getSelectedItem();
                boolean success = analyticsDAO.addItem(
                    nameField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    (String)categoryCombo.getSelectedItem(),
                    Double.parseDouble(ratingField.getText().trim()),
                    tagsField.getText().trim(),
                    descArea.getText().trim(),
                    subCatField.getText().trim(),
                    diet
                );

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Item added successfully!");
                    dialog.dispose();
                    refreshStats();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error saving item to database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid data input: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 12, 12, 12);
        dialog.add(saveBtn, gbc);

        dialog.setVisible(true);
    }

    private void addFormField(JDialog dialog, String label, JComponent field, int row, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Inter", Font.BOLD, 12));
        dialog.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        dialog.add(field, gbc);
    }
}
