package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {

    @NotNull(message = "Текст обязателен")
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 2000, message = "Текст комментария должен содержать от 1 до 2000 символов")
    private String text;
}