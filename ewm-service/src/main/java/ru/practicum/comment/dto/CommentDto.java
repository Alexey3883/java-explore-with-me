package ru.practicum.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long id;

    private EventShortDto event;

    private String text;

    private UserShortDto author;

    private LocalDateTime created;

    private LocalDateTime updated;
}