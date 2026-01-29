package com.gptini.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room_message_counters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomMessageCounterEntity {

    @Id
    private Long roomId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "room_id")
    private ChatRoomEntity chatRoom;

    @Column(nullable = false)
    @Builder.Default
    private Long lastMessageId = 0L;

    public Long getNextMessageId() {
        return ++lastMessageId;
    }
}
