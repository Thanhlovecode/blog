-- Fix camelCase column names in profiles table to follow snake_case convention
-- Only rename if camelCase columns still exist (idempotent)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'profiles' AND COLUMN_NAME = 'imageUrl');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE profiles CHANGE COLUMN `imageUrl` image_url VARCHAR(255)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'profiles' AND COLUMN_NAME = 'imageId');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE profiles CHANGE COLUMN `imageId` image_id VARCHAR(100)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
