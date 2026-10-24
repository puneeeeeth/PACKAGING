INSERT INTO teams (team_id, team_name)
SELECT 1, 'Team 1' WHERE NOT EXISTS (SELECT 1 FROM teams WHERE team_id = 1);
INSERT INTO teams (team_id, team_name)
SELECT 2, 'Team 2' WHERE NOT EXISTS (SELECT 1 FROM teams WHERE team_id = 2);
INSERT INTO teams (team_id, team_name)
SELECT 3, 'Team 3' WHERE NOT EXISTS (SELECT 1 FROM teams WHERE team_id = 3);
INSERT INTO teams (team_id, team_name)
SELECT 4, 'Team 4' WHERE NOT EXISTS (SELECT 1 FROM teams WHERE team_id = 4);

INSERT INTO app_metadata (metadata_key, metadata_value)
SELECT 'demo-seed-state', 'PENDING'
WHERE NOT EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state')
  AND NOT EXISTS (SELECT 1 FROM employees)
  AND NOT EXISTS (SELECT 1 FROM attendance)
  AND NOT EXISTS (SELECT 1 FROM parts)
  AND NOT EXISTS (SELECT 1 FROM marks)
  AND NOT EXISTS (SELECT 1 FROM shifts)
  AND NOT EXISTS (SELECT 1 FROM production_entries);

INSERT INTO employees (employee_number, employee_name, team_id, is_active)
SELECT 'DEMO-101', 'Sample Operator 01', 1, TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO employees (employee_number, employee_name, team_id, is_active)
SELECT 'DEMO-102', 'Sample Operator 02', 1, TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO employees (employee_number, employee_name, team_id, is_active)
SELECT 'DEMO-201', 'Sample Operator 03', 2, TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO employees (employee_number, employee_name, team_id, is_active)
SELECT 'DEMO-202', 'Sample Operator 04', 2, TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO employees (employee_number, employee_name, team_id, is_active)
SELECT 'DEMO-301', 'Sample Operator 05', 3, TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO employees (employee_number, employee_name, team_id, is_active)
SELECT 'DEMO-302', 'Sample Operator 06', 3, TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO employees (employee_number, employee_name, team_id, is_active)
SELECT 'DEMO-401', 'Sample Operator 07', 4, TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO employees (employee_number, employee_name, team_id, is_active)
SELECT 'DEMO-402', 'Sample Operator 08', 4, TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');

INSERT INTO parts (part_number, part_name, is_active)
SELECT 'DEMO-PACK-01', 'Sample carton', TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO parts (part_number, part_name, is_active)
SELECT 'DEMO-PACK-02', 'Sample shipping box', TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO shifts (shift_name, is_active)
SELECT 'Day Shift', TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO shifts (shift_name, is_active)
SELECT 'Night Shift', TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');

INSERT INTO marks (mark_name, is_active)
SELECT 'AA', TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO marks (mark_name, is_active)
SELECT 'KIT', TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO marks (mark_name, is_active)
SELECT 'Main', TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO marks (mark_name, is_active)
SELECT 'Htz', TRUE
WHERE EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');

INSERT INTO attendance (employee_id, attendance_date, status)
SELECT employee_id, CURRENT_DATE, 'P' FROM employees
WHERE employee_number = 'DEMO-101' AND EXISTS
    (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO attendance (employee_id, attendance_date, status)
SELECT employee_id, CURRENT_DATE, 'A' FROM employees
WHERE employee_number = 'DEMO-102' AND EXISTS
    (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO attendance (employee_id, attendance_date, status)
SELECT employee_id, CURRENT_DATE, 'P' FROM employees
WHERE employee_number = 'DEMO-201' AND EXISTS
    (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO attendance (employee_id, attendance_date, status)
SELECT employee_id, CURRENT_DATE, 'P' FROM employees
WHERE employee_number = 'DEMO-202' AND EXISTS
    (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO attendance (employee_id, attendance_date, status)
SELECT employee_id, CURRENT_DATE, 'A' FROM employees
WHERE employee_number = 'DEMO-301' AND EXISTS
    (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO attendance (employee_id, attendance_date, status)
SELECT employee_id, CURRENT_DATE, 'P' FROM employees
WHERE employee_number = 'DEMO-302' AND EXISTS
    (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO attendance (employee_id, attendance_date, status)
SELECT employee_id, CURRENT_DATE, 'P' FROM employees
WHERE employee_number = 'DEMO-401' AND EXISTS
    (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO attendance (employee_id, attendance_date, status)
SELECT employee_id, CURRENT_DATE, 'A' FROM employees
WHERE employee_number = 'DEMO-402' AND EXISTS
    (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');

INSERT INTO production_entries (production_date, operator_name, part_id, mark_id, production_quantity, remarks)
SELECT CURRENT_DATE, 'Sample Operator 01', p.part_id, m.mark_id, 1200, 'DEMO SAMPLE - replace with actual production'
FROM parts p, marks m
WHERE p.part_number = 'DEMO-PACK-01' AND m.mark_name = 'AA'
  AND EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO production_entries (production_date, operator_name, part_id, mark_id, production_quantity, remarks)
SELECT CURRENT_DATE, 'Sample Operator 02', p.part_id, m.mark_id, 850, 'DEMO SAMPLE - replace with actual production'
FROM parts p, marks m
WHERE p.part_number = 'DEMO-PACK-02' AND m.mark_name = 'KIT'
  AND EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');
INSERT INTO production_entries (production_date, operator_name, part_id, mark_id, production_quantity, remarks)
SELECT CURRENT_DATE, 'Sample Operator 03', p.part_id, m.mark_id, 460, 'DEMO SAMPLE - replace with actual production'
FROM parts p, marks m
WHERE p.part_number = 'DEMO-PACK-01' AND m.mark_name = 'Main'
  AND EXISTS (SELECT 1 FROM app_metadata WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING');

UPDATE marks SET mark_name = 'AA'
WHERE mark_name = 'DEMO-MARK-A' AND NOT EXISTS (SELECT 1 FROM marks WHERE mark_name = 'AA');
UPDATE marks SET mark_name = 'KIT'
WHERE mark_name = 'DEMO-MARK-B' AND NOT EXISTS (SELECT 1 FROM marks WHERE mark_name = 'KIT');
INSERT INTO marks (mark_name, is_active)
SELECT 'Main', TRUE
WHERE EXISTS (SELECT 1 FROM parts WHERE part_number LIKE 'DEMO-%')
  AND NOT EXISTS (SELECT 1 FROM marks WHERE mark_name = 'Main');
INSERT INTO marks (mark_name, is_active)
SELECT 'Htz', TRUE
WHERE EXISTS (SELECT 1 FROM parts WHERE part_number LIKE 'DEMO-%')
  AND NOT EXISTS (SELECT 1 FROM marks WHERE mark_name = 'Htz');
UPDATE production_entries SET operator_name = 'Sample Operator 01'
WHERE operator_name = 'Unassigned' AND remarks LIKE 'DEMO SAMPLE%'
  AND part_id = (SELECT part_id FROM parts WHERE part_number = 'DEMO-PACK-01');
UPDATE production_entries SET operator_name = 'Sample Operator 02'
WHERE operator_name = 'Unassigned' AND remarks LIKE 'DEMO SAMPLE%'
  AND part_id = (SELECT part_id FROM parts WHERE part_number = 'DEMO-PACK-02');

UPDATE app_metadata SET metadata_value = 'DONE'
WHERE metadata_key = 'demo-seed-state' AND metadata_value = 'PENDING';
