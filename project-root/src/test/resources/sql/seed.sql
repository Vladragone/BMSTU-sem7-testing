INSERT INTO location_groups (name)
SELECT 'Default Group'
WHERE NOT EXISTS (SELECT 1 FROM location_groups WHERE name = 'Default Group');
