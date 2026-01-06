-- Migration script to add department column to doctors table
-- Run this script on your PostgreSQL database

-- Step 1: Add department column (nullable first to allow migration)
ALTER TABLE doctors 
ADD COLUMN IF NOT EXISTS department VARCHAR(50);

-- Step 2: Migrate existing specialization data to department (if any)
-- Map common specialization values to department enum values
UPDATE doctors 
SET department = CASE 
    WHEN LOWER(specialization) LIKE '%cardiology%' OR LOWER(specialization) LIKE '%tim mạch%' THEN 'CARDIOLOGY'
    WHEN LOWER(specialization) LIKE '%neurology%' OR LOWER(specialization) LIKE '%thần kinh%' THEN 'NEUROLOGY'
    WHEN LOWER(specialization) LIKE '%pediatrics%' OR LOWER(specialization) LIKE '%nhi%' THEN 'PEDIATRICS'
    WHEN LOWER(specialization) LIKE '%surgery%' OR LOWER(specialization) LIKE '%ngoại%' THEN 'SURGERY'
    WHEN LOWER(specialization) LIKE '%obstetrics%' OR LOWER(specialization) LIKE '%gynecology%' OR LOWER(specialization) LIKE '%sản%' OR LOWER(specialization) LIKE '%phụ%' THEN 'OBSTETRICS_GYNECOLOGY'
    WHEN LOWER(specialization) LIKE '%orthopedic%' OR LOWER(specialization) LIKE '%chấn thương%' THEN 'ORTHOPEDICS'
    WHEN LOWER(specialization) LIKE '%oncology%' OR LOWER(specialization) LIKE '%ung bướu%' THEN 'ONCOLOGY'
    WHEN LOWER(specialization) LIKE '%gastroenterology%' OR LOWER(specialization) LIKE '%tiêu hóa%' THEN 'GASTROENTEROLOGY'
    WHEN LOWER(specialization) LIKE '%respiratory%' OR LOWER(specialization) LIKE '%hô hấp%' THEN 'RESPIRATORY'
    WHEN LOWER(specialization) LIKE '%nephrology%' OR LOWER(specialization) LIKE '%thận%' THEN 'NEPHROLOGY'
    WHEN LOWER(specialization) LIKE '%endocrinology%' OR LOWER(specialization) LIKE '%nội tiết%' THEN 'ENDOCRINOLOGY'
    WHEN LOWER(specialization) LIKE '%hematology%' OR LOWER(specialization) LIKE '%huyết học%' THEN 'HEMATOLOGY'
    WHEN LOWER(specialization) LIKE '%rheumatology%' OR LOWER(specialization) LIKE '%cơ xương%' THEN 'RHEUMATOLOGY'
    WHEN LOWER(specialization) LIKE '%dermatology%' OR LOWER(specialization) LIKE '%da liễu%' THEN 'DERMATOLOGY'
    WHEN LOWER(specialization) LIKE '%infectious%' OR LOWER(specialization) LIKE '%truyền nhiễm%' THEN 'INFECTIOUS_DISEASE'
    WHEN LOWER(specialization) LIKE '%general%' OR LOWER(specialization) LIKE '%nội tổng quát%' OR LOWER(specialization) LIKE '%lâm sàng%' THEN 'GENERAL_MEDICINE'
    ELSE 'GENERAL_MEDICINE' -- Default to GENERAL_MEDICINE if no match
END
WHERE department IS NULL AND specialization IS NOT NULL;

-- Step 3: Set default value for any remaining NULL departments
UPDATE doctors 
SET department = 'GENERAL_MEDICINE'
WHERE department IS NULL;

-- Step 4: Make department column NOT NULL (after migration)
ALTER TABLE doctors 
ALTER COLUMN department SET NOT NULL;

-- Step 5: Add check constraint to ensure only valid enum values
ALTER TABLE doctors 
ADD CONSTRAINT doctors_department_check 
CHECK (department IN (
    'GENERAL_MEDICINE',
    'PEDIATRICS',
    'OBSTETRICS_GYNECOLOGY',
    'SURGERY',
    'CARDIOLOGY',
    'NEUROLOGY',
    'ORTHOPEDICS',
    'ONCOLOGY',
    'GASTROENTEROLOGY',
    'RESPIRATORY',
    'NEPHROLOGY',
    'ENDOCRINOLOGY',
    'HEMATOLOGY',
    'RHEUMATOLOGY',
    'DERMATOLOGY',
    'INFECTIOUS_DISEASE'
));

-- Note: The specialization column is kept for backward compatibility
-- You can drop it later if not needed:
-- ALTER TABLE doctors DROP COLUMN IF EXISTS specialization;



