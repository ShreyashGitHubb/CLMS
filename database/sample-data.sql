USE clms;

INSERT INTO users (full_name, email, password_hash, role) VALUES
('Aarav Kulkarni', 'aarav@clms.edu', 'demo-password', 'STUDENT'),
('Meera Joshi', 'meera@clms.edu', 'demo-password', 'LAB_ASSISTANT'),
('System Admin', 'admin@clms.edu', 'demo-password', 'ADMIN');

INSERT INTO equipment (name, category, description, total_quantity, available_quantity, location, equipment_condition, status) VALUES
('Digital Multimeter', 'Measurement', 'Handheld digital multimeter for electronics experiments.', 18, 14, 'Electronics Lab', 'GOOD', 'AVAILABLE'),
('Arduino Uno Kit', 'Microcontroller', 'Arduino board with sensors, jumper wires, and USB cable.', 12, 3, 'Embedded Systems Lab', 'GOOD', 'LOW_STOCK'),
('Oscilloscope', 'Measurement', 'Two-channel digital oscilloscope for signal analysis.', 6, 6, 'Electronics Lab', 'GOOD', 'AVAILABLE'),
('Soldering Station', 'Workshop', 'Temperature-controlled soldering station.', 8, 0, 'Workshop A', 'FAIR', 'MAINTENANCE'),
('Raspberry Pi 4 Kit', 'Computer', 'Raspberry Pi board with power supply and case.', 10, 7, 'Computer Lab 2', 'GOOD', 'AVAILABLE');

INSERT INTO borrow_transactions (user_id, equipment_id, issue_date, due_date, status, fine_amount, approved_by) VALUES
(1, 2, NULL, '2026-09-28', 'PENDING', 0.00, NULL),
(1, 1, '2026-09-18', '2026-09-21', 'OVERDUE', 10.00, 2);

INSERT INTO maintenance_logs (equipment_id, reported_by, issue_description, reported_date, status) VALUES
(4, 2, 'Heating element requires replacement before the next practical.', '2026-09-20', 'IN_PROGRESS');
