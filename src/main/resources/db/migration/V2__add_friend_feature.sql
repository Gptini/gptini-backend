-- V2__add_friend_feature.sql
-- 친구 기능 추가

-- users 테이블에 friend_code 추가
ALTER TABLE users ADD COLUMN friend_code VARCHAR(8) UNIQUE;

-- 기존 유저들에게 랜덤 코드 부여 (8자리 영문대문자+숫자)
-- 실제로는 애플리케이션에서 처리하거나, 별도 스크립트로 처리 필요
-- UPDATE users SET friend_code = ... WHERE friend_code IS NULL;

-- friend_code NOT NULL로 변경 (기존 데이터 처리 후)
-- ALTER TABLE users MODIFY COLUMN friend_code VARCHAR(8) NOT NULL;

-- 친구 요청 테이블
CREATE TABLE friend_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_requester_receiver (requester_id, receiver_id),
    INDEX idx_friend_requests_receiver (receiver_id),
    INDEX idx_friend_requests_status (status),
    CONSTRAINT fk_friend_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_requests_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 친구 관계 테이블
CREATE TABLE friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    INDEX idx_friendships_friend (friend_id),
    CONSTRAINT fk_friendships_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
