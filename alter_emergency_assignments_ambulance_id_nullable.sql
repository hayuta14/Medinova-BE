-- Script to make ambulance_id nullable in emergency_assignments table
-- This allows emergency assignments without an ambulance (doctor only)

-- Step 1: Drop the NOT NULL constraint (if exists)
ALTER TABLE emergency_assignments 
ALTER COLUMN ambulance_id DROP NOT NULL;

-- Verify the change
-- SELECT column_name, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'emergency_assignments' 
-- AND column_name = 'ambulance_id';

