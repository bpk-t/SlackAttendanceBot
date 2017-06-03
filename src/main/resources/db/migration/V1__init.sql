
CREATE TABLE attendance (
    id IDENTITY PRIMARY KEY,
    att_date DATE NOT NULL,
    att_date_time TIMESTAMP,
    att_type VARCHAR(16) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    user_name VARCHAR(64) NOT NULL,
    break_time_min INTEGER,
    created_at timestamp NOT NULL
);