package ru.practicum.comment.dto;

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
    @Size(min = 1, max = 2000)
    private String text;
}