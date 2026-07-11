INSERT INTO screening_rules
    (id, name, field, match_value, action, active, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Synthetic blocked creditor',
     'CREDITOR_ACCOUNT', 'TEST-BLOCKED-001', 'REJECT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000002', 'Synthetic country review',
     'DESTINATION_COUNTRY', 'ZZ', 'MANUAL_REVIEW', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
