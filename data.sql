INSERT INTO users (email, full_name, password, username, enabled, created_at)
VALUES
    ('admin@fifo.local', 'System Administrator', '$2a$10$walcATdoZ/tgw5rEzI496.aAmTzyE.669SOn7pdrIl3XEWQAufW2m', 'admin', TRUE, CURRENT_TIMESTAMP),
    ('manager@fifo.local', 'Project Manager', '$2a$10$TZ4p1jC88/KNU18/AeE.0ejwl7/SIoB2y36grd/3yVFOivQubKT5S', 'manager', TRUE, CURRENT_TIMESTAMP),
    ('employee1@fifo.local', 'Employee One', '$2a$10$V61xN7YkUyLu1yliAN78yeGKw62nBW0xetmLK4.UIudyVyjvG.Hc.', 'employee1', TRUE, CURRENT_TIMESTAMP),
    ('employee2@fifo.local', 'Employee Two', '$2a$10$V61xN7YkUyLu1yliAN78yeGKw62nBW0xetmLK4.UIudyVyjvG.Hc.', 'employee2', TRUE, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE email = VALUES(email);

INSERT INTO user_roles (user_id, roles)
VALUES
    ((SELECT id FROM users WHERE email = 'admin@fifo.local'), 'ADMIN'),
    ((SELECT id FROM users WHERE email = 'manager@fifo.local'), 'MANAGER'),
    ((SELECT id FROM users WHERE email = 'employee1@fifo.local'), 'EMPLOYEE'),
    ((SELECT id FROM users WHERE email = 'employee2@fifo.local'), 'EMPLOYEE')
ON DUPLICATE KEY UPDATE roles = VALUES(roles);
