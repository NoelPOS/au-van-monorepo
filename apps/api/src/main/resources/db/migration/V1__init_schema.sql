-- =============================================================
-- AU Van — Initial Database Schema
-- V1__init_schema.sql
-- =============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================
-- USERS
-- =============================================================
CREATE TABLE users (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email            VARCHAR(255) UNIQUE,
    password_hash    VARCHAR(255),
    line_user_id     VARCHAR(255) UNIQUE,
    auth_provider    VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    display_name     VARCHAR(255),
    name             VARCHAR(255) NOT NULL,
    phone            VARCHAR(50),
    default_pickup_location VARCHAR(500),
    profile_image_url       VARCHAR(500),
    is_admin         BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email        ON users (email)        WHERE email IS NOT NULL;
CREATE INDEX idx_users_line_user_id ON users (line_user_id) WHERE line_user_id IS NOT NULL;
CREATE INDEX idx_users_is_admin     ON users (is_admin);

-- =============================================================
-- ROUTES
-- =============================================================
CREATE TABLE routes (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    from_location    VARCHAR(255) NOT NULL,
    to_location      VARCHAR(255) NOT NULL,
    slug             VARCHAR(255) NOT NULL UNIQUE,
    price            NUMERIC(10,2) NOT NULL,
    duration_minutes INTEGER,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_routes_status ON routes (status);

-- =============================================================
-- TIMESLOTS
-- =============================================================
CREATE TABLE timeslots (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id     UUID        NOT NULL REFERENCES routes (id) ON DELETE CASCADE,
    date         DATE        NOT NULL,
    time         TIME        NOT NULL,
    total_seats  INTEGER     NOT NULL CHECK (total_seats BETWEEN 1 AND 50),
    booked_seats INTEGER     NOT NULL DEFAULT 0 CHECK (booked_seats >= 0),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (route_id, date, time)
);

CREATE INDEX idx_timeslots_route_date ON timeslots (route_id, date);
CREATE INDEX idx_timeslots_status     ON timeslots (status);

-- =============================================================
-- SEATS
-- =============================================================
CREATE TABLE seats (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    timeslot_id  UUID        NOT NULL REFERENCES timeslots (id) ON DELETE CASCADE,
    seat_number  INTEGER     NOT NULL,
    label        VARCHAR(10) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    locked_by    UUID        REFERENCES users (id) ON DELETE SET NULL,
    locked_at    TIMESTAMPTZ,
    booked_by    UUID        REFERENCES users (id) ON DELETE SET NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (timeslot_id, seat_number)
);

CREATE INDEX idx_seats_timeslot ON seats (timeslot_id);
CREATE INDEX idx_seats_status   ON seats (status);
CREATE INDEX idx_seats_locked   ON seats (locked_at) WHERE status = 'LOCKED';

-- =============================================================
-- BOOKINGS
-- NOTE: payment_id FK is added after payments table is created
-- =============================================================
CREATE TABLE bookings (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID        NOT NULL REFERENCES users (id),
    route_id                    UUID        NOT NULL REFERENCES routes (id),
    timeslot_id                 UUID        NOT NULL REFERENCES timeslots (id),
    passengers                  INTEGER     NOT NULL CHECK (passengers >= 1),
    passenger_name              VARCHAR(255) NOT NULL,
    passenger_phone             VARCHAR(50)  NOT NULL,
    pickup_location             VARCHAR(500) NOT NULL,
    status                      VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    payment_id                  UUID,  -- FK added below
    payment_due_at              TIMESTAMPTZ,
    booking_code                VARCHAR(20)  UNIQUE,
    source_channel              VARCHAR(20)  DEFAULT 'LIFF',
    rescheduled_from_booking_id UUID         REFERENCES bookings (id) ON DELETE SET NULL,
    total_price                 NUMERIC(10,2) NOT NULL,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_bookings_user_id       ON bookings (user_id);
CREATE INDEX idx_bookings_timeslot_id   ON bookings (timeslot_id);
CREATE INDEX idx_bookings_status        ON bookings (status);
CREATE INDEX idx_bookings_payment_due   ON bookings (payment_due_at) WHERE payment_due_at IS NOT NULL;
CREATE INDEX idx_bookings_user_created  ON bookings (user_id, created_at DESC);

-- =============================================================
-- BOOKING_SEATS  (join table)
-- =============================================================
CREATE TABLE booking_seats (
    booking_id UUID NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    seat_id    UUID NOT NULL REFERENCES seats   (id) ON DELETE CASCADE,
    PRIMARY KEY (booking_id, seat_id)
);

-- =============================================================
-- PAYMENTS
-- =============================================================
CREATE TABLE payments (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id          UUID         NOT NULL REFERENCES bookings (id),
    user_id             UUID         NOT NULL REFERENCES users (id),
    amount              NUMERIC(10,2) NOT NULL,
    method              VARCHAR(20)  NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    transaction_id      VARCHAR(255),
    proof_image_url     VARCHAR(500),
    proof_reference     VARCHAR(255),
    proof_submitted_at  TIMESTAMPTZ,
    reviewed_by         UUID         REFERENCES users (id) ON DELETE SET NULL,
    reviewed_at         TIMESTAMPTZ,
    review_note         TEXT,
    paid_at             TIMESTAMPTZ,
    refunded_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_booking_id ON payments (booking_id);
CREATE INDEX idx_payments_user_id    ON payments (user_id);
CREATE INDEX idx_payments_status     ON payments (status);

-- Now that payments exists, add the FK from bookings
ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_payment_id
    FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE SET NULL;

-- =============================================================
-- NOTIFICATIONS
-- =============================================================
CREATE TABLE notifications (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES users (id),
    type            VARCHAR(50) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         TEXT        NOT NULL,
    read            BOOLEAN     NOT NULL DEFAULT FALSE,
    channel         VARCHAR(20) NOT NULL DEFAULT 'INAPP',
    delivery_status VARCHAR(20) DEFAULT 'PENDING',
    data            JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_unread ON notifications (user_id, read, created_at DESC);

-- =============================================================
-- REMINDER_JOBS
-- =============================================================
CREATE TABLE reminder_jobs (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id   UUID        NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users (id),
    line_user_id VARCHAR(255),
    type         VARCHAR(30) NOT NULL,
    run_at       TIMESTAMPTZ NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts     INTEGER     NOT NULL DEFAULT 0,
    locked_at    TIMESTAMPTZ,
    sent_at      TIMESTAMPTZ,
    last_error   TEXT,
    payload      JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (booking_id, type)
);

CREATE INDEX idx_reminder_jobs_status_run_at ON reminder_jobs (status, run_at);

-- =============================================================
-- AUDIT_LOGS
-- =============================================================
CREATE TABLE audit_logs (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id    UUID         REFERENCES users (id) ON DELETE SET NULL,
    action      VARCHAR(100) NOT NULL,
    target_type VARCHAR(50)  NOT NULL,
    target_id   VARCHAR(100),
    metadata    JSONB,
    ip          VARCHAR(45),
    user_agent  TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_actor_id   ON audit_logs (actor_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);

-- =============================================================
-- IDEMPOTENCY_KEYS
-- =============================================================
CREATE TABLE idempotency_keys (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL REFERENCES users (id),
    scope          VARCHAR(100) NOT NULL,
    key_value      VARCHAR(255) NOT NULL,
    request_hash   VARCHAR(64)  NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    response_status INTEGER,
    response_data  JSONB,
    error_message  TEXT,
    expires_at     TIMESTAMPTZ  NOT NULL DEFAULT (now() + INTERVAL '24 hours'),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (user_id, scope, key_value)
);

CREATE INDEX idx_idempotency_expires_at ON idempotency_keys (expires_at);
