package com.gptini.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChatMessageIdEntity implements Serializable {
    private Long messageId;
    private Long roomId;
}
