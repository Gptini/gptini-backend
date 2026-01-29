-- V1__init_schema.sql
-- GPTini 초기 스키마

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    profile_image_url VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅방 테이블
CREATE TABLE chat_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_chat_rooms_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅방 참여자 테이블
CREATE TABLE chat_room_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    last_read_message_id BIGINT NOT NULL DEFAULT 0,
    joined_at DATETIME NOT NULL,
    UNIQUE KEY uk_chat_room_user (chat_room_id, user_id),
    INDEX idx_chat_room_users_user_id (user_id),
    CONSTRAINT fk_chat_room_users_room FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_room_users_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅방 메시지 카운터 테이블
CREATE TABLE chat_room_message_counters (
    room_id BIGINT PRIMARY KEY,
    last_message_id BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_message_counter_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅 메시지 테이블
CREATE TABLE chat_messages (
    message_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    content TEXT,
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    created_at DATETIME NOT NULL,
    PRIMARY KEY (message_id, room_id),
    INDEX idx_chat_messages_room_id (room_id),
    INDEX idx_chat_messages_created_at (created_at),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
