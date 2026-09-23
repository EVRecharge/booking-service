CREATE TABLE IF NOT EXISTS service_bookings (
    booking_id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    booking_date TIMESTAMP NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_in_hours INTEGER,
    status VARCHAR(50) NOT NULL,
    total_cost DOUBLE PRECISION,
    notes TEXT,
    address VARCHAR(500),
    city VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON service_bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_provider_id ON service_bookings(provider_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON service_bookings(status);
