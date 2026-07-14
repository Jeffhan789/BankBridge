-- Seed users for v0.3.0 authentication
-- Default password == username (e.g., operator001 / operator001)
-- Generated with: htpasswd -bnB -C 12 <username> <username>

INSERT INTO users (id, username, password_hash, role, enabled) VALUES
('00000000-0000-0000-0000-000000000001', 'operator001', '$2y$12$9OViLJRdDRc4p/5X7U/Uc.GYsDEOMK.tq/O195i4rWuRnQWzvb7ci', 'OPERATOR', TRUE),
('00000000-0000-0000-0000-000000000002', 'compliance001', '$2y$12$fVwcP0fSyW.wxuyQViZleu7wJC4USVP44gI0a23i3fVebqxfmzB16', 'COMPLIANCE_ANALYST', TRUE),
('00000000-0000-0000-0000-000000000003', 'auditor001', '$2y$12$mCM0drr5sY.FQgNOu/E5eeU7vGL0O9pr7./f78H/I5hWAsXsUoksy', 'AUDITOR', TRUE),
('00000000-0000-0000-0000-000000000004', 'admin001', '$2y$12$Wz6sPgzC6LmvLWmmgJC.deNIzPuwHxM./9XJNnpvzUyoMyxYaDAj2', 'ADMIN', TRUE);
