# 친구 기능 설계

## 결정 사항
- **친구 관계**: 양방향 (요청 → 수락 → 서로 친구)
- **친구 코드**: 8자리 랜덤 자동 생성 (영문대문자+숫자)

---

## DB 스키마

### users 테이블 수정
```sql
ALTER TABLE users ADD COLUMN friend_code VARCHAR(8) UNIQUE NOT NULL;
```

### friend_requests 테이블 (새로 생성)
```sql
CREATE TABLE friend_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_requester_receiver (requester_id, receiver_id),
    FOREIGN KEY (requester_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

### friendships 테이블 (새로 생성)
```sql
CREATE TABLE friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (friend_id) REFERENCES users(id)
);
-- 수락시 양쪽에 row 2개 생성 (A->B, B->A)
```

---

## API

```
# 내 친구 코드
GET  /api/v1/users/me/friend-code        # 내 친구 코드 조회

# 유저 검색
GET  /api/v1/users/search?code=XXX       # 코드로 유저 검색 (미리보기)

# 친구 요청
GET  /api/v1/friend-requests             # 받은 요청 목록
POST /api/v1/friend-requests             # 요청 보내기 { "friendCode": "A1B2C3D4" }
PUT  /api/v1/friend-requests/{id}/accept # 수락
PUT  /api/v1/friend-requests/{id}/reject # 거절

# 친구
GET  /api/v1/friends                     # 내 친구 목록
DELETE /api/v1/friends/{friendId}        # 친구 삭제
```

---

## 구현 순서

- [x] 1. User 엔티티에 friendCode 추가
- [x] 2. Flyway 마이그레이션 작성 (V2)
- [x] 3. FriendRequest 엔티티 생성
- [x] 4. Friendship 엔티티 생성
- [x] 5. FriendRequestRepository 생성
- [x] 6. FriendshipRepository 생성
- [x] 7. FriendService 생성
- [x] 8. FriendController 생성
- [x] 9. 프론트엔드 친구 탭 추가

---

## 생성된 파일

### 엔티티
- `entity/FriendRequestStatus.java` - PENDING, ACCEPTED, REJECTED
- `entity/FriendRequest.java` - 친구 요청
- `entity/Friendship.java` - 친구 관계

### Repository
- `repository/FriendRequestRepository.java`
- `repository/FriendshipRepository.java`

### DTO
- `dto/request/FriendRequestRequest.java`
- `dto/response/FriendRequestResponse.java`
- `dto/response/FriendResponse.java`

### Service
- `service/FriendService.java`
- `service/FriendServiceImpl.java`

### Controller
- `controller/friend/FriendController.java`

### Util
- `util/FriendCodeGenerator.java` - 8자리 랜덤 코드 생성

### Migration
- `V2__add_friend_feature.sql`
