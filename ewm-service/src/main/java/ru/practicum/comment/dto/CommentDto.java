package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(min = 1, max = 1000)
    private String text;

    private UserShortDto author;

    private LocalDateTime created;

    private LocalDateTime updated;
}