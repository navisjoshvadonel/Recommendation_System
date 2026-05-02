# Precision Recommendation System v5.0

A modern desktop recommendation engine with premium cyan/teal UI (Ethereal Precision design) built from scratch using 100% Pure Java Core + Swing + MySQL.

## Features
- **Ethereal Precision UI**: Glass cards, modern typography, responsive components.
- **Smart Queries**: Extracts tags and domain preferences seamlessly for TF-IDF + rules-based scoring.
- **Multi-dataset ingestion**: Admin panel allows importing massive CSV datasets across varied domains (Education, Entertainment, Food, Jobs, Shopping, Travel).
- **Asynchronous tasks**: Heavy IO and JDBC insertions are managed by background thread pools, keeping the UI smooth.
- **Security**: Robust salt + SHA-256 password storage.

## Database Setup
1. Log into your MySQL environment.
2. Ensure you have created the required database (see `implementation.md` or original prompt file for schema).
```sql
CREATE DATABASE recommendation_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
3. Set your root/user password as an environment variable in your run session:
```bash
export DB_PASSWORD=your_mysql_root_password
```

## Running the Application
A shell script is provided to compile everything using the downloaded MySQL connector (`mysql-connector-j-9.1.0.jar` by default, serving as a replacement for 9.5.0).
```bash
./run_app.sh
```

## Import Data
- Log in or Register as a normal user.
- To access the Admin Panel, set a user's role to `admin` in the MySQL table:
  `UPDATE users SET role = 'admin' WHERE username = 'myadmin';`
- Import any CSV files located under the `Dataset/` folder.
