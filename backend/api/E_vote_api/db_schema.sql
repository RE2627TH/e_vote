-- Database Creation
CREATE DATABASE IF NOT EXISTS e_vote_db;
USE e_vote_db;

-- 1. Users Table (Stores all students, candidates, and admins)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dob DATE NOT NULL,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    department VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- 'student', 'candidate', 'admin'
    status VARCHAR(50) DEFAULT 'approved', -- 'approved', 'pending', 'rejected'
    profile_photo VARCHAR(255) DEFAULT NULL,
    fcm_token TEXT DEFAULT NULL,
    val_otp VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    screen VARCHAR(100) DEFAULT NULL,
    data_id VARCHAR(100) DEFAULT NULL,
    is_read TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Candidate Details Table (Stores specific details for candidates)
CREATE TABLE IF NOT EXISTS candidate_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    manifesto TEXT,
    photo VARCHAR(255) DEFAULT 'default_candidate.jpg',
    symbol VARCHAR(255) DEFAULT 'default_symbol.jpg',
    course VARCHAR(100),
    college VARCHAR(255),
    tagline VARCHAR(255),
    goals TEXT,
    pledges TEXT,
    symbol_name VARCHAR(255),
    status VARCHAR(50) DEFAULT 'NOT_SUBMITTED', -- 'NOT_SUBMITTED', 'SUBMITTED', 'ACCEPTED', 'REJECTED'
    application_completed TINYINT DEFAULT 0, -- 0 = draft, 1 = submitted
    rejection_reason TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Elections Table (Stores election events)
CREATE TABLE IF NOT EXISTS elections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    status VARCHAR(50) DEFAULT 'active', -- 'active', 'closed'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Votes Table (Stores votes cast by students)
CREATE TABLE IF NOT EXISTS votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    voter_id INT NOT NULL,
    candidate_id INT NOT NULL,
    position VARCHAR(100) NOT NULL,
    election_id INT DEFAULT 1,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voter_id) REFERENCES users(id),
    FOREIGN KEY (candidate_id) REFERENCES users(id),
    UNIQUE KEY unique_vote (voter_id, position, election_id) -- One vote per position per election
);

-- 5. Feedback Table (Stores feedback for candidates)
CREATE TABLE IF NOT EXISTS feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    user_name VARCHAR(255), 
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidate_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert Default Admin User
-- Password is 'admin123' (hashed)
INSERT INTO users (name, dob, student_id, department, email, password, role, status)
VALUES ('Admin User', '2005-02-27', 'ADMIN001', 'IT', 'rethikasenthilkumar76@gmail.com', '$2y$10$jULNTAIW4/6/zjJxRkJfxe.XXheB5hJ4VbbulrtvejoZloXpQLz9S', 'admin', 'approved');
