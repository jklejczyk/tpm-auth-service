-- demo users

INSERT INTO users (id, username, password_hash, role)
VALUES ('op-1', 'operator', '$2b$10$kyJIPVScrn4z/bJ/APfCtOXT1I.3Qu8QfUwcLcqWgWaI9Gm3S6pd2', 'OPERATOR'),
       ('tech-1', 'technik', '$2b$10$KJvRgfwkZQjyDdfGnmtmm.Q/tAP1d4eXfCbNH4UYrMOjfbT4W5m5a', 'TECHNICIAN'),
       ('mgr-1', 'kierownik', '$2b$10$fZCFtMDv7ekvsV3Gr.54S.R6FAXnRvs5QCVBmVHcDZscOYhvONr9O', 'MANAGER');
