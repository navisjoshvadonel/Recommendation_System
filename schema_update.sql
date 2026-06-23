-- Schema Update for Professional Recommendation System

-- 1. Create FULLTEXT Index for high-performance semantic searching
-- Note: Requires InnoDB engine and MySQL 5.6+
ALTER TABLE items ADD FULLTEXT INDEX ft_idx_items (name, tags, description);

-- 2. Create User Interactions Table for Collaborative Filtering / Implicit Feedback
CREATE TABLE IF NOT EXISTS user_interactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    interaction_type VARCHAR(50) NOT NULL, -- e.g., 'like', 'view'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    UNIQUE KEY unique_interaction (user_id, item_id, interaction_type)
);
