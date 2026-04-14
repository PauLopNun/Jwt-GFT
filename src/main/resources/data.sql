INSERT INTO users (username, password, role) VALUES
('alice@bookstore.dev', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ROLE_USER'),
('bob@bookstore.dev',   '$2a$10$TwNeO3bELbVMnFhVRlW2d.H1B0l4jLXk5L0aKTPf1a3LFDwOp0IYa', 'ROLE_ADMIN');

INSERT INTO books (title, author, isbn, price, stock) VALUES
('Clean Code', 'Robert C. Martin', '978-0132350884', 35.99, 10),
('Effective Java', 'Joshua Bloch', '978-0134685991', 45.00, 8),
('Spring in Action', 'Craig Walls', '978-1617294945', 49.99, 6);

