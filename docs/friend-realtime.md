# 친구 요청 실시간 WebSocket 구현

## 목표
- 친구 요청/수락/거절 시 실시간 알림
- 새로고침 없이 친구 목록 자동 업데이트

---

## 구독 토픽

### `/sub/users/{userId}/friends`
친구 관련 실시간 업데이트용

**수신 데이터:**
```json
{
  "type": "FRIEND_REQUEST" | "FRIEND_ACCEPTED" | "FRIEND_REJECTED",
  "friendRequest": {  // type이 FRIEND_REQUEST일 때
    "id": 1,
    "requester": {
      "id": 2,
      "nickname": "홍길동",
      "profileImageUrl": null
    },
    "status": "PENDING",
    "createdAt": "2024-01-29T12:00:00"
  },
  "friend": {  // type이 FRIEND_ACCEPTED일 때
    "id": 2,
    "nickname": "홍길동",
    "email": "hong@example.com",
    "profileImageUrl": null,
    "friendSince": "2024-01-29T12:00:00"
  },
  "requestId": 1  // type이 FRIEND_REJECTED일 때
}
```

---

## 이벤트별 동작

| 이벤트 | 발신자 화면 | 수신자 화면 |
|--------|------------|------------|
| 친구 요청 | - | 받은 요청 목록에 추가 |
| 요청 수락 | 친구 목록에 추가, 보낸 요청에서 제거 | 친구 목록에 추가 |
| 요청 거절 | 보낸 요청에서 제거 | - |

---

## 구현 체크리스트

### Phase 1: 백엔드

1. [ ] `FriendUpdateMessage` DTO 생성
   - 파일: `dto/response/FriendUpdateMessage.java`
   ```java
   public record FriendUpdateMessage(
       String type,  // FRIEND_REQUEST, FRIEND_ACCEPTED, FRIEND_REJECTED
       FriendRequestResponse friendRequest,
       FriendResponse friend,
       Long requestId
   ) {}
   ```

2. [ ] `FriendService`에 WebSocket 전송 로직 추가
   - `sendFriendRequest()`: 수신자에게 FRIEND_REQUEST 전송
   - `acceptRequest()`: 요청자에게 FRIEND_ACCEPTED 전송
   - `rejectRequest()`: 요청자에게 FRIEND_REJECTED 전송

3. [ ] `FriendController` 또는 `FriendServiceImpl`에서 `SimpMessageSendingOperations` 사용

### Phase 2: 프론트엔드 - Store

4. [ ] WebSocket 연결 시점을 `PrivateRoute`로 이동
   - 로그인 성공 후 어느 페이지에서든 알림 수신 가능
   - `ChatRoomListPage`, `ChatRoomPage`에서 connect() 호출 제거
   ```typescript
   // PrivateRoute.tsx
   useEffect(() => {
     if (user) {
       connect(user.id)
     }
   }, [user])
   ```

5. [ ] `chatStore.ts`에 친구 업데이트 상태 추가
   ```typescript
   // 상태 추가
   friendUpdates: FriendUpdate[]

   // 액션 추가
   subscribeToFriendUpdates: (userId: number) => void
   clearFriendUpdates: () => void
   ```

6. [ ] `connect()` 함수에서 친구 토픽도 함께 구독
   - `/sub/users/{userId}/rooms` (기존)
   - `/sub/users/{userId}/friends` (추가)

### Phase 3: 프론트엔드 - 적용

7. [ ] `FriendsPage`에서 실시간 업데이트 반영
   - `FRIEND_REQUEST`: receivedRequests에 추가
   - `FRIEND_ACCEPTED`: friends에 추가, sentRequests에서 제거
   - `FRIEND_REJECTED`: sentRequests에서 제거

8. [ ] (선택) 알림 토스트 표시
   - "홍길동님이 친구 요청을 보냈습니다"
   - "홍길동님이 친구 요청을 수락했습니다"

### Phase 4: 테스트

9. [ ] 다중 브라우저로 친구 요청 실시간 테스트
10. [ ] 수락/거절 시 양쪽 화면 동기화 테스트

---

## 데이터 흐름

### 시나리오 1: 친구 요청

```
[유저 A가 유저 B에게 친구 요청]
       ↓
[서버: FriendService.sendFriendRequest()]
       ↓
[DB에 FriendRequest 저장]
       ↓
[/sub/users/{B의 userId}/friends 로 FRIEND_REQUEST 전송]
       ↓
[유저 B의 FriendsPage]
       ↓
[받은 요청 목록에 자동 추가]
```

### 시나리오 2: 요청 수락

```
[유저 B가 유저 A의 요청 수락]
       ↓
[서버: FriendService.acceptRequest()]
       ↓
[DB에 Friendship 저장, FriendRequest 상태 변경]
       ↓
[/sub/users/{A의 userId}/friends 로 FRIEND_ACCEPTED 전송]
       ↓
[유저 A의 FriendsPage]
       ↓
[친구 목록에 유저 B 추가, 보낸 요청에서 제거]
```

---

## 참고: 기존 코드 위치

- 백엔드 친구 서비스: `service/FriendServiceImpl.java`
- 백엔드 친구 컨트롤러: `controller/friend/FriendController.java`
- 프론트엔드 친구 페이지: `pages/FriendsPage.tsx`
- 프론트엔드 WebSocket 스토어: `stores/chatStore.ts`
