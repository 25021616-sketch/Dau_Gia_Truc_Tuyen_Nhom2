CREATE TABLE IF NOT EXISTS server_config (
    id INT PRIMARY KEY,
    ip_address VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert a default row if not exists
INSERT IGNORE INTO server_config (id, ip_address, port) VALUES (1, '127.0.0.1', 8080);
