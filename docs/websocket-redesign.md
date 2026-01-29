# WebSocket 아키텍처 리디자인

## 목표
- 단일 WebSocket 연결로 모든 채팅방 관리
- 채팅 목록에서 실시간 업데이트 (최신 메시지, 안읽은 수)
- 방 전환 시 재연결 없이 즉시 전환

---

## 현재 vs 변경 후

### 현재 구조
```
[ChatRoomPage] --연결--> /ws
                --구독--> /sub/chat/rooms/{roomId}
                --구독--> /sub/chat/rooms/{roomId}/read
```
- 채팅방 입장 시 연결, 퇴장 시 해제
- 채팅 목록에서는 WebSocket 미사용

### 변경 후 구조
```
[ChatRoomListPage] --연결--> /ws
                   --구독--> /sub/users/{userId}/rooms (목록 업데이트용)
                   --구독--> /sub/chat/rooms/{roomId} (참여중인 모든 방)

[ChatRoomPage] -- WebSocket 공유 (재연결 없음)
```

---

## 구독 토픽 설계

### 1. `/sub/users/{userId}/rooms` (NEW)
채팅방 목록 실시간 업데이트용

**수신 데이터:**
```json
{
  "type": "ROOM_UPDATE",
  "roomId": 1,
  "lastMessage": "안녕하세요",
  "lastMessageTime": "2024-01-29T12:00:00",
  "lastMessageSenderId": 2,
  "lastMessageSenderNickname": "홍길동",
  "unreadCount": 3
}
```

**발생 시점:**
- 해당 유저가 참여한 방에 새 메시지가 올 때
- 해당 유저의 안읽은 수가 변경될 때

### 2. `/sub/chat/rooms/{roomId}` (기존 유지)
개별 채팅방 메시지 수신용

**수신 데이터:** (기존과 동일)
```json
{
  "messageId": 1,
  "roomId": 1,
  "senderId": 2,
  "senderNickname": "홍길동",
  "type": "TEXT",
  "content": "안녕하세요",
  "createdAt": "2024-01-29T12:00:00",
  "unreadCount": 2
}
```

### 3. `/sub/chat/rooms/{roomId}/read` (기존 유지)
읽음 상태 업데이트용

---

## 백엔드 변경사항

### 1. 새 DTO 생성
```java
// RoomUpdateMessage.java
public record RoomUpdateMessage(
    String type,           // "ROOM_UPDATE"
    Long roomId,
    String lastMessage,
    LocalDateTime lastMessageTime,
    Long lastMessageSenderId,
    String lastMessageSenderNickname,
    Integer unreadCount
) {}
```

### 2. ChatMessageService 수정
메시지 저장 후 해당 방의 모든 참여자에게 ROOM_UPDATE 전송

```java
// 메시지 전송 시
public void sendMessage(...) {
    // 1. 기존: /sub/chat/rooms/{roomId}로 메시지 전송

    // 2. 추가: 방 참여자들에게 목록 업데이트 전송
    List<Long> participantIds = getParticipantIds(roomId);
    for (Long userId : participantIds) {
        if (!userId.equals(senderId)) {
            // 발신자 제외한 참여자에게 ROOM_UPDATE 전송
            sendRoomUpdate(userId, roomId, message);
        }
    }
}
```

### 3. 읽음 처리 시 unreadCount 업데이트 전송
읽음 처리 후 해당 유저에게 ROOM_UPDATE 전송 (unreadCount: 0)

---

## 프론트엔드 변경사항

### 1. 전역 WebSocket 관리 (새 파일)
```
src/
  stores/
    chatStore.ts       # 전역 채팅 상태 관리 (zustand)
  hooks/
    useGlobalChat.ts   # 전역 WebSocket 연결 훅
```

### 2. chatStore.ts 설계
```typescript
interface ChatState {
  // WebSocket 상태
  isConnected: boolean
  client: Client | null

  // 채팅방 목록 실시간 데이터
  roomUpdates: Map<number, RoomUpdate>  // roomId -> 최신 상태

  // 현재 보고 있는 방의 메시지
  currentRoomId: number | null
  messages: ChatMessage[]

  // Actions
  connect: () => void
  disconnect: () => void
  subscribeToRooms: (roomIds: number[]) => void
  setCurrentRoom: (roomId: number) => void
  sendMessage: (request: SendMessageRequest) => void
  markAsRead: (roomId: number, messageId: number) => void
}
```

### 3. ChatRoomListPage 변경
```typescript
// 변경 전
useEffect(() => {
  fetchRooms()  // REST API로만 조회
}, [])

// 변경 후
useEffect(() => {
  fetchRooms()
  connect()  // WebSocket 연결
  subscribeToUserRooms()  // /sub/users/{userId}/rooms 구독
  subscribeToAllRooms(rooms)  // 참여중인 모든 방 구독
}, [])

// roomUpdates가 변경되면 목록 자동 업데이트
const mergedRooms = useMemo(() => {
  return rooms.map(room => ({
    ...room,
    ...roomUpdates.get(room.id)  // 실시간 데이터로 덮어쓰기
  }))
}, [rooms, roomUpdates])
```

### 4. ChatRoomPage 변경
```typescript
// 변경 전: 자체 WebSocket 연결
const { isConnected, sendMessage } = useChatRoom({ roomId, ... })

// 변경 후: 전역 store 사용
const { isConnected, sendMessage, messages } = useChatStore()

useEffect(() => {
  setCurrentRoom(roomId)  // 현재 방 설정
  fetchMessages(roomId)   // 기존 메시지 로드
}, [roomId])
```

### 5. 삭제할 파일
- `src/hooks/useChatRoom.ts` → chatStore로 통합

---

## 데이터 흐름

### 시나리오 1: 새 메시지 수신 (채팅방 목록에 있을 때)

```
[유저 A가 방1에 메시지 전송]
       ↓
[서버: ChatMessageController.sendMessage]
       ↓
[1. /sub/chat/rooms/1 로 메시지 브로드캐스트]
       ↓
[2. 방1 참여자들에게 /sub/users/{userId}/rooms 로 ROOM_UPDATE 전송]
       ↓
[유저 B의 ChatRoomListPage]
       ↓
[roomUpdates 상태 업데이트]
       ↓
[목록 UI 자동 갱신 - 최신 메시지, 안읽은 수 표시]
```

### 시나리오 2: 새 메시지 수신 (채팅방 안에 있을 때)

```
[유저 A가 방1에 메시지 전송]
       ↓
[서버: /sub/chat/rooms/1 로 메시지 브로드캐스트]
       ↓
[유저 B의 ChatRoomPage (방1)]
       ↓
[messages 상태에 추가]
       ↓
[채팅 UI에 새 메시지 표시]
       ↓
[자동 읽음 처리 → unreadCount 0으로 업데이트]
```

### 시나리오 3: 방 전환

```
[유저 B: 방1 → 방2로 이동]
       ↓
[setCurrentRoom(2)]
       ↓
[messages를 방2의 메시지로 교체]
       ↓
[WebSocket 재연결 없음 - 이미 방2도 구독중]
```

---

## 구현 순서

### Phase 1: 백엔드
1. [x] `RoomUpdateMessage` DTO 생성
2. [x] `ChatMessageController`에 ROOM_UPDATE 전송 로직 추가
3. [x] 읽음 처리 시 ROOM_UPDATE 전송 로직 추가

### Phase 2: 프론트엔드 - Store
4. [x] `chatStore.ts` 생성 (zustand)
5. [x] 전역 WebSocket 연결/해제 로직
6. [x] roomUpdates 상태 관리

### Phase 3: 프론트엔드 - 적용
7. [x] `ChatRoomListPage` 수정 - 전역 WebSocket 연결, 실시간 목록 업데이트
8. [x] `ChatRoomPage` 수정 - 전역 store 사용
9. [x] `useChatRoom.ts` 삭제

### Phase 4: 테스트
10. [ ] 다중 브라우저로 실시간 동기화 테스트
11. [ ] 방 전환 시 연결 유지 테스트
12. [ ] 안읽은 수 정확성 테스트

---

## 고려사항

### 1. 초기 로딩 최적화
- 채팅 목록 진입 시 모든 방을 구독하면 부하가 클 수 있음
- 방안: 최근 활성화된 N개 방만 구독, 나머지는 목록 업데이트 토픽으로만 처리

### 2. 연결 끊김 처리
- 재연결 시 놓친 메시지 처리 필요
- 방안: 재연결 후 REST API로 최신 데이터 fetch

### 3. 메모리 관리
- 모든 방의 메시지를 메모리에 유지하면 과부하
- 방안: 현재 보고 있는 방의 메시지만 상세 유지, 나머지는 요약 정보만

### 4. 토큰 만료
- WebSocket 연결 중 토큰 만료 시 처리 필요
- 방안: 에러 발생 시 토큰 갱신 후 재연결
