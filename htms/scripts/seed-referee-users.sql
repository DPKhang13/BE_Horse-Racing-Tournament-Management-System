-- Development/test seed only. All five accounts use password: Admin@123
-- Password hashes are BCrypt (strength 10), compatible with Spring Security BCryptPasswordEncoder.
-- Run against PostgreSQL, for example:
--   psql -h <host> -U <user> -d <database> -f seed-referee-users.sql

BEGIN;

WITH referee_seed (username, email, password_hash, full_name, phone, license_number, address) AS (
    VALUES
        ('referee01', 'referee01@htms.local', '$2a$10$NAtpmgO7GJ25jLCHJLsTB.XYKlPhtzG5XgBRgPaecY4u328XLDP4a', 'Referee 01', '0900000001', 'REF-001', 'HTMS Track Office'),
        ('referee02', 'referee02@htms.local', '$2a$10$QDIKCNmFd19HVfnnRW9gmuSyYZ1VWNQIVCsrHyNHFD7/MOb18HwRK', 'Referee 02', '0900000002', 'REF-002', 'HTMS Track Office'),
        ('referee03', 'referee03@htms.local', '$2a$10$1ljqBC5EHWOjnvCBIcX6hurujQOsOjvkrFQIAxIJleer0PNRuKbt2', 'Referee 03', '0900000003', 'REF-003', 'HTMS Track Office'),
        ('referee04', 'referee04@htms.local', '$2a$10$f/YdH.hVwv4fzyGVKbzJ/e4e6S8DOPtLmPmhEJW/jwbUMdR.fSKA.', 'Referee 04', '0900000004', 'REF-004', 'HTMS Track Office'),
        ('referee05', 'referee05@htms.local', '$2a$10$UOpmCOWhIJh2vziAr2de0OIKwQQvSi4o4lVt0ZYqKrRhe.BQSvksq', 'Referee 05', '0900000005', 'REF-005', 'HTMS Track Office')
),
existing_referees AS (
    SELECT user_id, username
    FROM "users"
    WHERE username IN (SELECT username FROM referee_seed)
      AND role_type = 'race_referee'
),
inserted_users AS (
    INSERT INTO "users" (
        username,
        email,
        password_hash,
        full_name,
        phone,
        role_type,
        status,
        created_at
    )
    SELECT
        seed.username,
        seed.email,
        seed.password_hash,
        seed.full_name,
        seed.phone,
        'race_referee',
        'active',
        CURRENT_TIMESTAMP
    FROM referee_seed seed
    WHERE NOT EXISTS (
        SELECT 1
        FROM "users" existing_user
        WHERE existing_user.username = seed.username
           OR LOWER(existing_user.email) = LOWER(seed.email)
    )
    RETURNING user_id, username
),
referee_users AS (
    SELECT user_id, username FROM existing_referees
    UNION ALL
    SELECT user_id, username FROM inserted_users
)
INSERT INTO "referee_profiles" (
    referee_id,
    license_number,
    address,
    status,
    created_at
)
SELECT
    referee_user.user_id,
    seed.license_number,
    seed.address,
    'active',
    CURRENT_TIMESTAMP
FROM referee_seed seed
JOIN referee_users referee_user ON referee_user.username = seed.username
ON CONFLICT (referee_id) DO NOTHING;

COMMIT;

-- Verify the inserted accounts and their referee profiles.
SELECT
    user_record.user_id,
    user_record.username,
    user_record.email,
    user_record.role_type,
    user_record.status AS user_status,
    referee_profile.referee_id,
    referee_profile.license_number,
    referee_profile.status AS referee_status
FROM "users" user_record
LEFT JOIN "referee_profiles" referee_profile ON referee_profile.referee_id = user_record.user_id
WHERE user_record.username IN ('referee01', 'referee02', 'referee03', 'referee04', 'referee05')
ORDER BY user_record.username;
