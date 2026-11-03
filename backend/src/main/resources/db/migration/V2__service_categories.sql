-- Service catalog. Category names are content (not UI strings) and are stored in Vietnamese.

CREATE TABLE service_categories (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    code          VARCHAR(50)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(500) NULL,
    icon          VARCHAR(50)  NULL,
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    CONSTRAINT pk_service_categories PRIMARY KEY (id),
    CONSTRAINT uk_service_categories_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_service_categories_active_order ON service_categories (active, display_order);

INSERT INTO service_categories (code, name, description, icon, display_order, created_at, updated_at)
VALUES ('PLUMBING', 'Sửa ống nước', 'Rò rỉ, tắc nghẽn, lắp đặt thiết bị vệ sinh và đường ống.', 'droplets', 10, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('ELECTRICAL', 'Sửa điện', 'Chập điện, thay ổ cắm, công tắc, đèn và đi dây điện.', 'zap', 20, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('AIR_CONDITIONING', 'Điện lạnh', 'Vệ sinh, bảo dưỡng, nạp gas và sửa máy lạnh, tủ lạnh.', 'snowflake', 30, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('APPLIANCES', 'Thiết bị gia dụng', 'Sửa máy giặt, bình nóng lạnh, bếp điện và đồ gia dụng.', 'washing-machine', 40, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('CARPENTRY', 'Sửa đồ gỗ', 'Sửa cửa, tủ, bàn ghế và lắp đặt nội thất gỗ.', 'hammer', 50, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('PAINTING', 'Sơn nhà', 'Sơn tường, sơn chống thấm và xử lý tường ẩm mốc.', 'paint-roller', 60, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('LOCKSMITH', 'Sửa khóa', 'Mở khóa, thay ổ khóa và lắp khóa điện tử.', 'key-round', 70, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)),
       ('MASONRY', 'Xây dựng & chống thấm', 'Sửa nứt tường, ốp lát, chống thấm mái và nhà vệ sinh.', 'brick-wall', 80, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));
