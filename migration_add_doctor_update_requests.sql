-- Migration script to create doctor_update_requests table
-- Run this script on your PostgreSQL database

CREATE TABLE IF NOT EXISTS doctor_update_requests (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    clinic_id BIGINT,
    department VARCHAR(50),
    experience_years INTEGER,
    bio TEXT,
    default_start_time TIME,
    default_end_time TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    CONSTRAINT fk_doctor_update_request_doctor 
        FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    CONSTRAINT fk_doctor_update_request_clinic 
        FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE SET NULL,
    CONSTRAINT doctor_update_requests_status_check 
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_doctor_update_requests_doctor_id ON doctor_update_requests(doctor_id);
CREATE INDEX IF NOT EXISTS idx_doctor_update_requests_status ON doctor_update_requests(status);
CREATE INDEX IF NOT EXISTS idx_doctor_update_requests_doctor_status ON doctor_update_requests(doctor_id, status);



