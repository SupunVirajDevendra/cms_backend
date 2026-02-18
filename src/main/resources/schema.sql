-- =========================
-- MASTER TABLE: STATUS
-- =========================
CREATE TABLE status (
                        status_code VARCHAR(20) PRIMARY KEY,
                        description VARCHAR(100) NOT NULL
);

-- =========================
-- MASTER TABLE: CARD_REQUEST_TYPE
-- =========================
CREATE TABLE card_request_type (
                                   code VARCHAR(20) PRIMARY KEY,
                                   description VARCHAR(100) NOT NULL
);

-- =========================
-- TRANSACTION TABLE: CARD
-- =========================
CREATE TABLE card (
                      card_number VARCHAR(20) PRIMARY KEY,
                      expiry_date DATE NOT NULL,
                      status_code VARCHAR(20) NOT NULL,
                      credit_limit NUMERIC(15,2) NOT NULL CHECK (credit_limit >= 0),
                      cash_limit NUMERIC(15,2) NOT NULL CHECK (cash_limit >= 0),
                      available_credit_limit NUMERIC(15,2) NOT NULL CHECK (available_credit_limit >= 0),
                      available_cash_limit NUMERIC(15,2) NOT NULL CHECK (available_cash_limit >= 0),
                      last_update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      CONSTRAINT fk_card_status
                          FOREIGN KEY (status_code)
                              REFERENCES status(status_code)
);

-- =========================
-- TRANSACTION TABLE: CARD_REQUEST
-- =========================
CREATE TABLE card_request (
                              request_id SERIAL PRIMARY KEY,
                              card_number VARCHAR(20) NOT NULL,
                              request_reason_code VARCHAR(20) NOT NULL,
                              status_code VARCHAR(20) NOT NULL,
                              create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_request_card
                                  FOREIGN KEY (card_number)
                                      REFERENCES card(card_number)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_request_type
                                  FOREIGN KEY (request_reason_code)
                                      REFERENCES card_request_type(code),

                              CONSTRAINT fk_request_status
                                  FOREIGN KEY (status_code)
                                      REFERENCES status(status_code)
);

-- =========================
-- INDEXES (Performance)
-- =========================
CREATE INDEX idx_card_status ON card(status_code);
CREATE INDEX idx_request_status ON card_request(status_code);
CREATE INDEX idx_request_card ON card_request(card_number);
