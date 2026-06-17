-- DDL for fault image metadata table (optional, for production persistence)
-- For demo: in-memory ConcurrentHashMap is used; add this table for persistence.

CREATE TABLE IF NOT EXISTS fault_image (
    image_id    BIGINT PRIMARY KEY,
    inspection_id BIGINT NOT NULL,
    file_path   VARCHAR(500) NOT NULL,
    created_at  BIGINT NOT NULL,
    INDEX idx_inspection (inspection_id)
);

-- Spring Boot application.properties additions:
--
-- image.storage.path=./upload/images
-- spring.servlet.multipart.max-file-size=10MB
-- spring.servlet.multipart.max-request-size=20MB
