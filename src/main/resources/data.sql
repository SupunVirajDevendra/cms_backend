-- ===============================
-- CARD STATUS MASTER DATA
-- ===============================

INSERT INTO card_status (status_code, description) VALUES
                                                       ('IACT', 'Inactive'),
                                                       ('CACT', 'Active'),
                                                       ('DACT', 'Deactivated');

-- ===============================
-- REQUEST STATUS MASTER DATA
-- ===============================

INSERT INTO request_status (status_code, description) VALUES
                                                          ('PENDING', 'Pending'),
                                                          ('APPROVED', 'Approved'),
                                                          ('REJECTED', 'Rejected');

-- ===============================
-- CARD REQUEST TYPES
-- ===============================

INSERT INTO card_request_type (code, description) VALUES
                                                      ('ACTI', 'Card Activation'),
                                                      ('CDCL', 'Card Close');

-- ===============================
-- SAMPLE CARDS
-- ===============================

INSERT INTO card (
    card_number,
    expiry_date,
    status_code,
    credit_limit,
    cash_limit,
    available_credit_limit,
    available_cash_limit,
    last_update_time
) VALUES
      ('4111111111111111', '2027-12-31', 'IACT', 100000.00, 50000.00, 100000.00, 50000.00, CURRENT_TIMESTAMP),
      ('4222222222222222', '2026-06-30', 'CACT', 200000.00, 100000.00, 200000.00, 100000.00, CURRENT_TIMESTAMP);
