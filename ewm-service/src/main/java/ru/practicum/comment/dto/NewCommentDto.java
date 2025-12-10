package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.comment.validation.ValidCommentText;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {

    @ValidCommentText
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 2000, message = "Текст комментария должен содержать от 1 до 2000 символов")
    private String text;
}